<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

use Illuminate\Support\Facades\Storage;

class EmergencyAlert extends Model
{
    use HasFactory, HasUuids;

    protected $fillable = [
        'user_id',
        'type',
        'status',
        'audio_path',
        'expires_at',
    ];

    protected $casts = [
        'expires_at' => 'datetime',
    ];

    protected $appends = [
        'audio_url',
    ];

    public function getAudioUrlAttribute(): ?string
    {
        if (!$this->audio_path) {
            return null;
        }

        if (request()->httpHost()) {
            $isSecure = request()->secure() || (strtolower(request()->header('X-Forwarded-Proto', '')) === 'https');
            $scheme = $isSecure ? 'https' : 'http';
            return $scheme . '://' . request()->httpHost() . '/storage/' . $this->audio_path;
        }

        $appUrl = rtrim(config('app.url', env('APP_URL', 'https://api.estoyok24.com')), '/');
        return $appUrl . '/storage/' . $this->audio_path;
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function responses(): HasMany
    {
        return $this->hasMany(EmergencyResponse::class);
    }
}
