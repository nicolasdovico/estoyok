<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Facades\DB;

class CurrentLocation extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'accuracy',
        'recorded_at',
        'location',
    ];

    protected $appends = ['latitude', 'longitude'];

    protected $hidden = ['location'];

    protected function casts(): array
    {
        return [
            'recorded_at' => 'datetime',
            'latitude' => 'float',
            'longitude' => 'float',
        ];
    }

    protected static function booted()
    {
        static::addGlobalScope('coordinates', function (Builder $builder) {
            $builder->addSelect([
                'current_locations.*',
                DB::raw('ST_Y(location::geometry) as latitude'),
                DB::raw('ST_X(location::geometry) as longitude'),
            ]);
        });
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function getLatitudeAttribute()
    {
        return $this->attributes['latitude'] ?? null;
    }

    public function getLongitudeAttribute()
    {
        return $this->attributes['longitude'] ?? null;
    }

    public static function whereUser(int $userId)
    {
        return self::where('user_id', $userId)->first();
    }
}
