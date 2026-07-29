<?php

namespace App\Models;

use Database\Factories\UserFactory;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Cashier\Billable;
use Laravel\Sanctum\HasApiTokens;

use Filament\Models\Contracts\FilamentUser;
use Filament\Panel;

class User extends Authenticatable implements MustVerifyEmail, FilamentUser
{
    /** @use HasFactory<UserFactory> */
    use Billable, HasApiTokens, HasFactory, Notifiable;

    /**
     * The "booted" method of the model.
     */
    protected static function booted(): void
    {
        static::saving(function (User $user) {
            if ($user->subscription_status === 'inactive') {
                $user->is_premium = false;
                $user->trial_ends_at = null;
                $user->billing_cycle_ends_at = null;
                $user->trial_reminder_sent_at = null;
                $user->grace_period_ends_at = null;
                $user->mp_subscription_id = null;
                $user->mp_status = null;
                $user->paypal_subscription_id = null;
                $user->paypal_status = null;
            }
        });

        static::saved(function (User $user) {
            if (in_array($user->subscription_status, ['inactive', 'canceled']) && method_exists($user, 'subscriptions')) {
                $user->subscriptions()->delete();
            }
        });
    }

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
        'avatar_path',
        'trial_ends_at',
        'subscription_provider',
        'subscription_id',
        'subscription_status',
        'billing_cycle_ends_at',
        'trial_reminder_sent_at',
        'grace_period_ends_at',
        'disclaimer_accepted_at',
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
     * The attributes that should be appended to the model's array form.
     *
     * @var array<int, string>
     */
    protected $appends = [
        'avatar_url',
        'is_on_trial',
        'trial_days_left',
        'has_premium_access',
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
            'trial_ends_at' => 'datetime',
            'billing_cycle_ends_at' => 'datetime',
            'trial_reminder_sent_at' => 'datetime',
            'grace_period_ends_at' => 'datetime',
            'disclaimer_accepted_at' => 'datetime',
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
     * Determine if user has premium access via explicit setting, active trial, or active subscription.
     */
    public function getHasPremiumAccessAttribute(): bool
    {
        return $this->hasPremiumAccess();
    }

    /**
     * Determine if user is currently in their 7-day free trial.
     */
    public function getIsOnTrialAttribute(): bool
    {
        return $this->trial_ends_at !== null && $this->trial_ends_at->isFuture();
    }

    /**
     * Get remaining trial days.
     */
    public function getTrialDaysLeftAttribute(): int
    {
        if (!$this->is_on_trial || !$this->trial_ends_at) {
            return 0;
        }

        return (int) max(0, ceil(now()->diffInSeconds($this->trial_ends_at, false) / 86400));
    }

    /**
     * Determine if the user has an active premium subscription or active trial.
     */
    public function hasPremiumAccess(): bool
    {
        if (in_array($this->subscription_status, ['inactive', 'canceled'])) {
            return (bool) $this->is_premium;
        }

        return $this->is_premium ||
               $this->is_on_trial ||
               in_array($this->subscription_status, ['active', 'trialing', 'grace_period']) ||
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

    /**
     * Determine if the user can access the Filament admin panel.
     */
    public function canAccessPanel(Panel $panel): bool
    {
        return str_ends_with($this->email, '@estoyok.com') || $this->email === 'nicolasdovico@gmail.com';
    }

    /**
     * Get the user's avatar URL.
     */
    public function getAvatarUrlAttribute(): ?string
    {
        if (!$this->avatar_path) {
            return null;
        }

        if (request()->httpHost()) {
            $isSecure = request()->secure() || (strtolower(request()->header('X-Forwarded-Proto', '')) === 'https');
            $scheme = $isSecure ? 'https' : 'http';
            return $scheme . '://' . request()->httpHost() . '/storage/' . $this->avatar_path;
        }

        return url('/storage/' . $this->avatar_path);
    }
}
