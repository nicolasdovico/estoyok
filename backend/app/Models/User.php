<?php

namespace App\Models;

use Database\Factories\UserFactory;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Cashier\Billable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable implements MustVerifyEmail
{
    /** @use HasFactory<UserFactory> */
    use Billable, HasApiTokens, HasFactory, Notifiable;

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'name',
        'email',
        'password',
        'last_check_in_at',
        'checkin_interval_hours',
        'expo_push_token',
        'is_premium',
        'phone',
        'mp_subscription_id',
        'mp_status',
        'paypal_subscription_id',
        'paypal_status',
        'last_reminder_sent_at',
        'quiet_hours_enabled',
        'quiet_hours_start',
        'quiet_hours_end',
        'timezone',
        'allow_sms_whatsapp_checkin',
        'escalation_enabled',
        'escalation_interval_minutes',
        'share_contact_responses',
        'wifi_checkin_enabled',
        'safe_wifi_ssid',
        'sensor_checkin_enabled',
        'low_battery_alerts_enabled',
        'last_battery_alert_sent_at',
        'proximity_alerts_enabled',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
            'last_check_in_at' => 'datetime',
            'checkin_interval_hours' => 'integer',
            'is_premium' => 'boolean',
            'last_reminder_sent_at' => 'datetime',
            'quiet_hours_enabled' => 'boolean',
            'allow_sms_whatsapp_checkin' => 'boolean',
            'escalation_enabled' => 'boolean',
            'escalation_interval_minutes' => 'integer',
            'share_contact_responses' => 'boolean',
            'wifi_checkin_enabled' => 'boolean',
            'sensor_checkin_enabled' => 'boolean',
            'low_battery_alerts_enabled' => 'boolean',
            'last_battery_alert_sent_at' => 'datetime',
            'proximity_alerts_enabled' => 'boolean',
        ];
    }

    public function circles()
    {
        return $this->belongsToMany(Circle::class)->withPivot('role')->withTimestamps();
    }

    public function ownedCircles()
    {
        return $this->hasMany(Circle::class, 'owner_id');
    }

    public function emergencyContacts()
    {
        return $this->hasMany(EmergencyContact::class);
    }

    public function currentLocation()
    {
        return $this->hasOne(CurrentLocation::class);
    }

    public function locationHistories()
    {
        return $this->hasMany(LocationHistory::class);
    }

    public function emergencyAlerts()
    {
        return $this->hasMany(EmergencyAlert::class);
    }

    public function activeEmergencyAlerts()
    {
        return $this->hasMany(EmergencyAlert::class)->where('status', 'active');
    }

    public function checkIns()
    {
        return $this->hasMany(CheckIn::class);
    }

    public function driveEvents()
    {
        return $this->hasMany(DriveEvent::class);
    }

    public function crashEvents()
    {
        return $this->hasMany(CrashEvent::class);
    }

    public function initiatedDynamicGeofences()
    {
        return $this->hasMany(DynamicGeofence::class, 'initiator_id');
    }

    public function targetedDynamicGeofences()
    {
        return $this->hasMany(DynamicGeofence::class, 'target_id');
    }

    /**
     * Determine if the user is currently in their quiet hours.
     */
    public function isInQuietHours(): bool
    {
        if (!$this->quiet_hours_enabled || !$this->quiet_hours_start || !$this->quiet_hours_end) {
            return false;
        }

        $timezone = $this->timezone ?? config('app.timezone', 'America/Argentina/Buenos_Aires');
        $now = now()->setTimezone($timezone);
        $currentTimeString = $now->format('H:i:s');

        $start = $this->quiet_hours_start;
        $end = $this->quiet_hours_end;

        if ($start <= $end) {
            // Normal range (e.g. 09:00 to 17:00)
            return $currentTimeString >= $start && $currentTimeString <= $end;
        } else {
            // Midnight crossover range (e.g. 23:00 to 07:00)
            return $currentTimeString >= $start || $currentTimeString <= $end;
        }
    }

    /**
     * Determine if the user has an active premium subscription.
     */
    public function hasPremiumAccess(): bool
    {
        return $this->is_premium ||
               $this->subscribed('default') ||
               in_array($this->mp_status, ['authorized', 'active']) ||
               in_array($this->paypal_status, ['active', 'approved']);
    }

    /**
     * Mutator to sanitize phone numbers by removing spaces, dashes, and parentheses.
     */
    public function setPhoneAttribute($value)
    {
        $this->attributes['phone'] = $value ? preg_replace('/[\s\-\(\)]+/', '', $value) : null;
    }
}
