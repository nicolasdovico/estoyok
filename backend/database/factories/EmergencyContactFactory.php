<?php

namespace Database\Factories;

use App\Models\EmergencyContact;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

class EmergencyContactFactory extends Factory
{
    protected $model = EmergencyContact::class;

    public function definition(): array
    {
        return [
            "user_id" => User::factory(),
            "name" => $this->faker->name,
            "phone" => $this->faker->e164PhoneNumber,
            "email" => $this->faker->safeEmail,
            "relationship" => $this->faker->randomElement(["Padre", "Madre", "Amigo", "Hermano", "Cónyuge"]),
            "is_active" => true,
        ];
    }
}
