<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class DynamicGeofence extends Model
{
    use HasFactory;

    protected $fillable = [
        'initiator_id',
        'target_id',
        'safe_radius_meters',
        'is_active',
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'safe_radius_meters' => 'integer',
    ];

    /**
     * Scope a query to only include active dynamic geofences.
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }

    /**
     * Get the initiator (tutor) of the geofence.
     */
    public function initiator(): BelongsTo
    {
        return $this->belongsTo(User::class, 'initiator_id');
    }

    /**
     * Get the target (child/family member) of the geofence.
     */
    public function target(): BelongsTo
    {
        return $this->belongsTo(User::class, 'target_id');
    }
}
