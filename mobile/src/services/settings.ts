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
  updatePrivacy: async (shareContactResponses?: boolean, lowBatteryAlertsEnabled?: boolean) => {
    const payload: any = {};
    if (shareContactResponses !== undefined) payload.share_contact_responses = shareContactResponses;
    if (lowBatteryAlertsEnabled !== undefined) payload.low_battery_alerts_enabled = lowBatteryAlertsEnabled;
    const response = await api.put('/settings/privacy', payload);
    return response.data;
  },
  updateAutomation: async (wifiEnabled: boolean, ssid: string, sensorEnabled: boolean) => {
    const response = await api.put('/settings/automation', {
      wifi_checkin_enabled: wifiEnabled,
      safe_wifi_ssid: ssid || null,
      sensor_checkin_enabled: sensorEnabled
    });
    return response.data;
  },
  fetchUserSettings: async () => {
    const response = await api.get('/user');
    return response.data;
  }
};
