package com.estoyok.app.features.wellbeing.data.model

import com.google.gson.annotations.SerializedName

data class CheckinIntervalRequest(
    @SerializedName("checkin_interval_hours") val checkinIntervalHours: Int
)

data class QuietHoursRequest(
    @SerializedName("quiet_hours_enabled") val enabled: Boolean,
    @SerializedName("quiet_hours_start") val start: String,
    @SerializedName("quiet_hours_end") val end: String,
    @SerializedName("timezone") val timezone: String
)

data class SmsWhatsappCheckinRequest(
    @SerializedName("allow_sms_whatsapp_checkin") val enabled: Boolean
)

data class EscalationRequest(
    @SerializedName("escalation_enabled") val enabled: Boolean,
    @SerializedName("escalation_interval_minutes") val intervalMinutes: Int
)

data class PrivacyRequest(
    @SerializedName("share_contact_responses") val shareContactResponses: Boolean? = null,
    @SerializedName("low_battery_alerts_enabled") val lowBatteryAlertsEnabled: Boolean? = null
)

data class AutomationRequest(
    @SerializedName("wifi_checkin_enabled") val wifiEnabled: Boolean,
    @SerializedName("safe_wifi_ssid") val safeWifiSsid: String?,
    @SerializedName("sensor_checkin_enabled") val sensorEnabled: Boolean
)

data class ProximityAlertsRequest(
    @SerializedName("proximity_alerts_enabled") val enabled: Boolean
)

data class PushTokenRequest(
    @SerializedName("push_token") val pushToken: String
)
