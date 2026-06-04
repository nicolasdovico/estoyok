import * as TaskManager from 'expo-task-manager';
import api from '@/services/api';

export const LOCATION_TASK_NAME = 'background-location-task';

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
      } catch (err) {
        // En segundo plano los logs son difíciles de ver, pero intentamos reportar
        console.error('Failed to update background location', err);
      }
    }
  }
});
