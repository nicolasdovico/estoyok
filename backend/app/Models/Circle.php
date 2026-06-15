<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Str;

class Circle extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'invite_code',
        'owner_id',
    ];

    protected static function boot()
    {
        parent::boot();

        static::creating(function ($circle) {
            if (! $circle->invite_code) {
                $circle->invite_code = Str::upper(Str::random(10));
            }
        });
    }

    public function owner()
    {
        return $this->belongsTo(User::class, 'owner_id');
    }

    public function users()
    {
        return $this->belongsToMany(User::class)->withPivot('role')->withTimestamps();
    }

    public function geofences()
    {
        return $this->hasMany(Geofence::class);
    }
}
