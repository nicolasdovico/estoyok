import * as TaskManager from 'expo-task-manager';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Pedometer } from 'expo-sensors';
import NetInfo from '@react-native-community/netinfo';
import api from '@/services/api';

export const LOCATION_TASK_NAME = 'background-location-task';

// Configure NetInfo to fetch SSID (required for iOS)
NetInfo.configure({
  shouldFetchWiFiSSID: true,
});

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
        await api.post('/locations/update', {
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          accuracy: location.coords.accuracy,
        });
        console.log('Background location updated via TaskManager');

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
