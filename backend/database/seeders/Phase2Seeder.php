<?php

namespace Database\Seeders;

use App\Models\Circle;
use App\Models\EmergencyContact;
use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class Phase2Seeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Crear usuario de prueba
        $user = User::create([
            'name' => 'Usuario de Prueba',
            'email' => 'test@example.com',
            'password' => Hash::make('password'),
            'phone' => '+541100000000',
            'last_check_in_at' => now()->subHours(25), // Ya vencido para probar el comando
            'created_at' => now()->subDays(2),
        ]);

        // Crear contacto de emergencia
        EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto de Emergencia 1',
            'phone' => '+541199999999',
            'email' => 'emergencia@example.com',
        ]);

        // Crear un círculo
        $circle = Circle::create([
            'name' => 'Familia Test',
            'invite_code' => 'TEST1234',
            'owner_id' => $user->id,
        ]);

        $user->circles()->attach($circle->id, ['role' => 'admin']);

        // Crear un admin para Filament
        User::create([
            'name' => 'Admin Estoy Ok',
            'email' => 'admin@estoyok.com',
            'password' => Hash::make('admin123'),
            'is_premium' => true,
        ]);
    }
}
