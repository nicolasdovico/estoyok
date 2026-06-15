<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Geofence extends Model
{
    protected $fillable = [
        'circle_id',
        'name',
        'radius',
        'type',
        'is_active',
        'center',
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'radius' => 'float',
    ];

    public function circle()
    {
        return $this->belongsTo(Circle::class);
    }
}
