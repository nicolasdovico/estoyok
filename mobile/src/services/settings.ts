import api from './api';

export const settingsService = {
  updateCheckinInterval: async (hours: number) => {
    const response = await api.put('/settings/checkin-interval', {
      checkin_interval_hours: hours
    });
    return response.data;
  },
  updateQuietHours: async (enabled: boolean, start: string, end: string, timezone: string) => {
    const response = await api.put('/settings/quiet-hours', {
      quiet_hours_enabled: enabled,
      quiet_hours_start: start,
      quiet_hours_end: end,
      timezone: timezone
    });
    return response.data;
  },
  updateSmsWhatsappCheckin: async (enabled: boolean) => {
    const response = await api.put('/settings/sms-whatsapp-checkin', {
      allow_sms_whatsapp_checkin: enabled
    });
    return response.data;
  },
  updateEscalation: async (enabled: boolean, minutes: number) => {
    const response = await api.put('/settings/escalation', {
      escalation_enabled: enabled,
      escalation_interval_minutes: minutes
    });
    return response.data;
  },
  updatePrivacy: async (enabled: boolean) => {
    const response = await api.put('/settings/privacy', {
      share_contact_responses: enabled
    });
    return response.data;
  },
  fetchUserSettings: async () => {
    const response = await api.get('/user');
    return response.data;
  }
};
