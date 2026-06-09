import api from './api';

export const settingsService = {
  updateCheckinInterval: async (hours: number) => {
    const response = await api.put('/settings/checkin-interval', {
      checkin_interval_hours: hours
    });
    return response.data;
  }
};
