<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class EmergencyContact extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'name',
        'phone',
        'email',
        'relationship',
        'is_active',
        'priority',
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'priority' => 'integer',
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Mutator to sanitize phone numbers by removing spaces, dashes, and parentheses.
     */
    public function setPhoneAttribute($value)
    {
        $this->attributes['phone'] = $value ? preg_replace('/[\s\-\(\)]+/', '', $value) : null;
    }
}
