<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class GeofenceEvent extends Model
{
    protected $fillable = [
        'user_id',
        'geofence_id',
        'type',
        'occurred_at',
    ];

    protected $casts = [
        'occurred_at' => 'datetime',
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function geofence()
    {
        return $this->belongsTo(Geofence::class);
    }
}
