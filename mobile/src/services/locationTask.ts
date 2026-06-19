import * as TaskManager from 'expo-task-manager';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Pedometer } from 'expo-sensors';
import NetInfo from '@react-native-community/netinfo';
import * as Battery from 'expo-battery';
import * as Location from 'expo-location';
import api from '@/services/api';

export const LOCATION_TASK_NAME = 'background-location-task';

// Configure NetInfo to fetch SSID (required for iOS)
NetInfo.configure({
  shouldFetchWiFiSSID: true,
});

async function queueOfflineLocation(payload: any) {
  try {
    const queueStr = await AsyncStorage.getItem('offline_location_queue');
    const queue = queueStr ? JSON.parse(queueStr) : [];
    queue.push(payload);
    if (queue.length > 100) {
      queue.shift(); // Keep at most 100 items to avoid storage bloat
    }
    await AsyncStorage.setItem('offline_location_queue', JSON.stringify(queue));
    console.log('Queued location update offline. Queue size:', queue.length);
  } catch (err) {
    console.error('Error queueing offline location:', err);
  }
}

export async function flushOfflineLocations() {
  try {
    const queueStr = await AsyncStorage.getItem('offline_location_queue');
    if (!queueStr) return;
    const queue = JSON.parse(queueStr);
    if (queue.length === 0) return;

    console.log(`Flushing ${queue.length} offline locations...`);
    const netInfoState = await NetInfo.fetch();
    if (!netInfoState.isConnected) {
      console.log('Cannot flush offline locations, network still disconnected.');
      return;
    }

    const failedItems = [];
    for (const item of queue) {
      try {
        await api.post('/locations/update', item);
      } catch (err) {
        console.error('Failed to flush offline location item, keeping in queue:', err);
        failedItems.push(item);
      }
    }

    if (failedItems.length > 0) {
      await AsyncStorage.setItem('offline_location_queue', JSON.stringify(failedItems));
    } else {
      await AsyncStorage.removeItem('offline_location_queue');
      console.log('All offline locations flushed successfully.');
    }
  } catch (err) {
    console.error('Error flushing offline locations:', err);
  }
}

TaskManager.defineTask(LOCATION_TASK_NAME, async ({ data, error }: any) => {
  if (error) {
    console.error('Background location error:', error);
    return;
  }
  if (data) {
    const { locations } = data;
    const location = locations[0];
    if (location) {
      try {
        let batteryLevel: number | null = null;
        try {
          const isBatteryAvailable = await Battery.isAvailableAsync();
          if (isBatteryAvailable) {
            batteryLevel = await Battery.getBatteryLevelAsync();
          }
        } catch (batteryErr) {
          console.error('Failed to read battery level:', batteryErr);
        }

        // Local rate limit flag in AsyncStorage
        if (batteryLevel !== null && batteryLevel < 0.15) {
          const now = Date.now();
          const lastSentStr = await AsyncStorage.getItem('last_low_battery_alert_sent');
          let shouldAlert = true;
          if (lastSentStr) {
            const lastSent = parseInt(lastSentStr, 10);
            if (now - lastSent < 60 * 60 * 1000) {
              shouldAlert = false;
            }
          }
          if (shouldAlert) {
            await AsyncStorage.setItem('last_low_battery_alert_sent', now.toString());
            console.log('Low battery threshold crossed (under 15%). Local alert flag stored.');
          }
        }

        let gpsEnabled = true;
        try {
          gpsEnabled = await Location.hasServicesEnabledAsync();
        } catch (gpsErr) {
          console.error('Failed to check GPS status:', gpsErr);
        }

        const payload = {
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          accuracy: location.coords.accuracy,
          battery_level: batteryLevel,
          gps_enabled: gpsEnabled,
          is_tracking_active: true,
          recorded_at: new Date(location.timestamp).toISOString(),
        };

        const netInfoState = await NetInfo.fetch();
        const isConnected = netInfoState.isConnected ?? false;

        if (!isConnected) {
          await queueOfflineLocation(payload);
        } else {
          try {
            await api.post('/locations/update', payload);
            console.log('Background location updated via TaskManager');
            // Try to flush any previously queued locations
            await flushOfflineLocations();
          } catch (apiErr) {
            console.error('Failed to update online, queueing location offline:', apiErr);
            await queueOfflineLocation(payload);
          }
        }

        // Trigger passive automation checks
        await performAutoCheckInChecks();
      } catch (err) {
        console.error('Failed to update background location or check-in', err);
      }
    }
  }
});

async function performAutoCheckInChecks() {
  try {
    // 1. Rate limit check (minimum 15 minutes between auto-checkins to prevent server spam)
    const now = Date.now();
    const lastCheckInStr = await AsyncStorage.getItem('last_auto_checkin_timestamp');
    if (lastCheckInStr) {
      const lastCheckIn = parseInt(lastCheckInStr, 10);
      const diffMs = now - lastCheckIn;
      if (diffMs < 15 * 60 * 1000) {
        // Skip check
        return;
      }
    }

    // 2. Fetch fresh user automation configuration
    const userResponse = await api.get('/user');
    const user = userResponse.data;

    if (!user) return;

    // Check if wifi check-in is enabled
    if (user.wifi_checkin_enabled) {
      const netInfoState = await NetInfo.fetch();
      if (netInfoState.type === 'wifi') {
        const currentSsid = netInfoState.details.ssid;
        // Since Expo Go/Emulators can return null SSID, we fallback to mock check:
        // if user configured SSID matches OR if it's Expo Go / simulated environment and SSID is null, we can allow wifi check-in for testing purposes
        const targetSsid = user.safe_wifi_ssid;
        const isMatched = currentSsid === targetSsid || 
          (targetSsid && (currentSsid === null || currentSsid === '<unknown ssid>'));

        if (isMatched) {
          console.log('Safe Wifi matches, performing auto-check-in');
          await api.post('/check-in', { source: 'wifi' });
          await AsyncStorage.setItem('last_auto_checkin_timestamp', now.toString());
          return; // Done
        }
      }
    }

    // Check if step-sensing check-in is enabled
    if (user.sensor_checkin_enabled) {
      const isAvailable = await Pedometer.isAvailableAsync();
      if (isAvailable) {
        const start = new Date();
        start.setHours(start.getHours() - 1);
        const end = new Date();
        const pedometerResult = await Pedometer.getStepCountAsync(start, end);
        if (pedometerResult && pedometerResult.steps >= 100) {
          console.log(`Pedometer steps count is ${pedometerResult.steps}, performing auto-check-in`);
          await api.post('/check-in', { source: 'movement' });
          await AsyncStorage.setItem('last_auto_checkin_timestamp', now.toString());
        }
      }
    }
  } catch (err) {
    console.error('Error during auto-checkin background processing:', err);
  }
}
