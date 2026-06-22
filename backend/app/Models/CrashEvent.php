<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CrashEvent extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'latitude',
        'longitude',
        'speed_at_impact',
        'g_force',
        'status',
    ];

    protected function casts(): array
    {
        return [
            'latitude' => 'float',
            'longitude' => 'float',
            'speed_at_impact' => 'float',
            'g_force' => 'float',
        ];
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
