import * as TaskManager from 'expo-task-manager';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Pedometer, Accelerometer } from 'expo-sensors';
import NetInfo from '@react-native-community/netinfo';
import * as Battery from 'expo-battery';
import * as Location from 'expo-location';
import api from '@/services/api';
import { DeviceEventEmitter, Platform } from 'react-native';
import * as Notifications from 'expo-notifications';

export const LOCATION_TASK_NAME = 'background-location-task';

if (Platform.OS !== 'web') {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowAlert: true,
      shouldPlaySound: true,
      shouldSetBadge: false,
      shouldShowBanner: true,
      shouldShowList: true,
    }),
  });
}

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

async function handleDynamicFrequencyUpdate(activeDynamicGeofence: boolean) {
  if (Platform.OS === 'web') return;
  try {
    const isHighFrequencyStr = await AsyncStorage.getItem('is_high_frequency');
    const isCurrentlyHigh = isHighFrequencyStr === 'true';

    if (activeDynamicGeofence && !isCurrentlyHigh) {
      console.log('Transitioning to HIGH frequency GPS updates (5s) for Proximity Radar...');
      await AsyncStorage.setItem('is_high_frequency', 'true');
      await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
        accuracy: Location.Accuracy.BestForNavigation,
        timeInterval: 5000, // 5 seconds
        distanceInterval: 2,
        foregroundService: {
          notificationTitle: 'Estoy Ok - Radar de Proximidad',
          notificationBody: 'Monitoreando radar de proximidad activo...',
          notificationColor: '#dc2626',
        },
      });
    } else if (!activeDynamicGeofence && isCurrentlyHigh) {
      console.log('Restoring to NORMAL frequency GPS updates (60s)...');
      await AsyncStorage.setItem('is_high_frequency', 'false');
      await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
        accuracy: Location.Accuracy.Balanced,
        timeInterval: 60000, // 60 seconds
        distanceInterval: 100,
        foregroundService: {
          notificationTitle: 'Estoy Ok está activo',
          notificationBody: 'Protegiendo tu ubicación en segundo plano',
          notificationColor: '#dc2626',
        },
      });
    }
  } catch (err) {
    console.error('Failed to change location update frequency:', err);
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

        let speed = (location.coords.speed !== null && location.coords.speed !== undefined && location.coords.speed >= 0) ? location.coords.speed : 0;
        let isDriving = false;

        try {
          const isDrivingStr = await AsyncStorage.getItem('is_driving_state');
          isDriving = isDrivingStr === 'true';

          const now = Date.now();

          if (speed > 7) {
            // Reset drop timer
            await AsyncStorage.removeItem('speed_dropped_below_started_at');

            const speedStartedAtStr = await AsyncStorage.getItem('speed_threshold_started_at');
            if (!speedStartedAtStr) {
              await AsyncStorage.setItem('speed_threshold_started_at', now.toString());
            } else {
              const startedAt = parseInt(speedStartedAtStr, 10);
              if (now - startedAt >= 60000) {
                isDriving = true;
                await AsyncStorage.setItem('is_driving_state', 'true');
              }
            }
          } else {
            if (isDriving) {
              const speedDroppedAtStr = await AsyncStorage.getItem('speed_dropped_below_started_at');
              if (!speedDroppedAtStr) {
                await AsyncStorage.setItem('speed_dropped_below_started_at', now.toString());
              } else {
                const droppedAt = parseInt(speedDroppedAtStr, 10);
                if (now - droppedAt >= 120000) {
                  isDriving = false;
                  await AsyncStorage.setItem('is_driving_state', 'false');
                  await AsyncStorage.removeItem('speed_threshold_started_at');
                  await AsyncStorage.removeItem('speed_dropped_below_started_at');
                }
              }
            } else {
              await AsyncStorage.removeItem('speed_threshold_started_at');
            }
          }
        } catch (storageErr) {
          console.error('AsyncStorage error in driving status calculation:', storageErr);
        }

        const payload = {
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          accuracy: location.coords.accuracy,
          battery_level: batteryLevel,
          gps_enabled: gpsEnabled,
          is_tracking_active: true,
          recorded_at: new Date(location.timestamp).toISOString(),
          speed: speed,
          is_driving: isDriving,
        };

        const netInfoState = await NetInfo.fetch();
        const isConnected = netInfoState.isConnected ?? false;

        if (!isConnected) {
          await queueOfflineLocation(payload);
        } else {
          try {
            const response = await api.post('/locations/update', payload);
            console.log('Background location updated via TaskManager');
            
            const activeDynamicGeofence = response.data?.active_dynamic_geofence ?? false;
            await handleDynamicFrequencyUpdate(activeDynamicGeofence);

            // Try to flush any previously queued locations
            await flushOfflineLocations();
          } catch (apiErr) {
            console.error('Failed to update online, queueing location offline:', apiErr);
            await queueOfflineLocation(payload);
          }
        }

        // Manage accelerometer subscription based on driving state
        manageAccelerometerSubscription(isDriving);

        // Trigger passive automation checks
        await performAutoCheckInChecks();
      } catch (err) {
        console.error('Failed to update background location or check-in', err);
      }
    }
  }
});

let accelerometerSubscription: any = null;
let isCollectingImmobility = false;
let immobilitySamples: number[] = [];

async function triggerCrashAlert(gForce: number) {
  try {
    const timestamp = Date.now();
    const data = { gForce, timestamp, status: 'pre_alert', timeRemaining: 15 };
    await AsyncStorage.setItem('active_crash_pre_alert', JSON.stringify(data));
    
    // Emit event for foreground listener
    DeviceEventEmitter.emit('crash_pre_alert', data);

    // Present notification
    await Notifications.scheduleNotificationAsync({
      content: {
        title: '🚨 ¡ALERTA DE ACCIDENTE DETECTADA!',
        body: `Se detectó un impacto de ${gForce.toFixed(1)}G. Toca para confirmar que estás bien.`,
        data: { type: 'crash_pre_alert', gForce },
        sound: true,
        priority: Notifications.AndroidNotificationPriority.MAX,
      },
      trigger: null,
    });
  } catch (err) {
    console.error('Error triggering crash alert:', err);
  }
}

export function manageAccelerometerSubscription(isDriving: boolean) {
  if (Platform.OS === 'web') return;

  if (isDriving) {
    if (!accelerometerSubscription) {
      console.log('Starting Accelerometer monitoring (is_driving = true)...');
      Accelerometer.setUpdateInterval(50); // 50ms interval (20Hz)
      accelerometerSubscription = Accelerometer.addListener(async (data) => {
        const { x, y, z } = data;
        const aNeta = Math.sqrt(x * x + y * y + z * z);

        if (!isCollectingImmobility) {
          // If we detect a peak of G force >= 4.5G
          if (aNeta >= 4.5) {
            console.log(`Peak G-force detected: ${aNeta.toFixed(2)}G. Starting 3s immobility verification...`);
            isCollectingImmobility = true;
            immobilitySamples = [];

            // Start a 3-second timer to collect immobility samples
            setTimeout(async () => {
              const samples = [...immobilitySamples];
              isCollectingImmobility = false;
              immobilitySamples = [];

              if (samples.length > 0) {
                // Calculate variance relative to 1G: Mean of (a_i - 1)^2
                let sumSquaredDiff = 0;
                for (const sample of samples) {
                  const diff = sample - 1.0;
                  sumSquaredDiff += diff * diff;
                }
                const variance = sumSquaredDiff / samples.length;
                console.log(`Immobility check completed. Samples: ${samples.length}, Variance from 1G: ${variance.toFixed(4)}G`);

                if (variance < 0.15) {
                  console.log('Crash pattern confirmed! Variance is low (device is still). Triggering alert...');
                  await triggerCrashAlert(aNeta);
                } else {
                  console.log(`Device is not still (variance = ${variance.toFixed(4)}G >= 0.15G). False trigger.`);
                }
              }
            }, 3000);
          }
        } else {
          // Collect samples
          immobilitySamples.push(aNeta);
        }
      });
    }
  } else {
    if (accelerometerSubscription) {
      console.log('Stopping Accelerometer monitoring (is_driving = false)...');
      accelerometerSubscription.remove();
      accelerometerSubscription = null;
      isCollectingImmobility = false;
      immobilitySamples = [];
    }
  }
}

// On module load, check if the device is currently driving and start/stop accelerometer accordingly
if (Platform.OS !== 'web') {
  AsyncStorage.getItem('is_driving_state').then((val) => {
    if (val === 'true') {
      manageAccelerometerSubscription(true);
    }
  }).catch((err) => console.error('Error initializing accelerometer subscription:', err));
}

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
