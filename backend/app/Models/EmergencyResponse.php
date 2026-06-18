<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class EmergencyResponse extends Model
{
    use HasFactory;

    protected $fillable = [
        'emergency_alert_id',
        'contact_name',
        'status',
    ];

    public function emergencyAlert(): BelongsTo
    {
        return $this->belongsTo(EmergencyAlert::class);
    }
}
