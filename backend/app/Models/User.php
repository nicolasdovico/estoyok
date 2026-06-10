<?php

namespace App\Models;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;
use Laravel\Cashier\Billable;

class User extends Authenticatable implements MustVerifyEmail
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, Notifiable, Billable;

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
}
