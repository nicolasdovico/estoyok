<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Geofence extends Model
{
    protected $fillable = [
        'circle_id',
        'user_id',
        'name',
        'radius',
        'type',
        'is_active',
        'center',
    ];

    protected $appends = ['latitude', 'longitude'];

    protected $hidden = ['center'];

    protected $casts = [
        'is_active' => 'boolean',
        'radius' => 'float',
        'user_id' => 'integer',
        'latitude' => 'float',
        'longitude' => 'float',
    ];

    protected static function booted()
    {
        static::addGlobalScope('coordinates', function (\Illuminate\Database\Eloquent\Builder $builder) {
            $builder->addSelect([
                'geofences.*',
                \Illuminate\Support\Facades\DB::raw('ST_Y(center::geometry) as latitude'),
                \Illuminate\Support\Facades\DB::raw('ST_X(center::geometry) as longitude'),
            ]);
        });
    }

    public function getLatitudeAttribute()
    {
        return $this->attributes['latitude'] ?? null;
    }

    public function getLongitudeAttribute()
    {
        return $this->attributes['longitude'] ?? null;
    }

    public function circle()
    {
        return $this->belongsTo(Circle::class);
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
