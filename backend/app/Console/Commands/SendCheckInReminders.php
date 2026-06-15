<?php

namespace App\Console\Commands;

use App\Mail\CheckInReminderMail;
use App\Models\User;
use Exception;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class SendCheckInReminders extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'checkins:send-reminders';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Enviar recordatorios preventivos a usuarios próximos a vencer su umbral de inactividad';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Iniciando envío de recordatorios de check-in...');

        $isLocal = app()->environment('local');
        $unit = $isLocal ? 'minutes' : 'hours';
        $offset = $isLocal ? '2 minutes' : '60 minutes';

        if ($isLocal) {
            $this->warn('ENTORNO LOCAL DETECTADO: Los intervalos se tratarán como MINUTOS y el offset será de 2 MINUTOS.');
        }

        // Buscamos usuarios que estén en la ventana de aviso:
        // Su último check-in (o creación si nunca hizo check-in) + umbral - offset <= NOW()
        // Y todavía no se ha vencido (último check-in + umbral > NOW())
        // Y no se les ha enviado un aviso en este ciclo (last_reminder_sent_at es nulo o anterior al último check-in / creación)
        $usersToRemind = User::where(function ($query) use ($unit, $offset) {
            $query->whereRaw("COALESCE(last_check_in_at, created_at) < NOW() - (checkin_interval_hours || ' {$unit}')::interval + '{$offset}'::interval")
                ->whereRaw("COALESCE(last_check_in_at, created_at) >= NOW() - (checkin_interval_hours || ' {$unit}')::interval");
        })
            ->where(function ($query) {
                $query->whereNull('last_reminder_sent_at')
                    ->orWhereRaw('last_reminder_sent_at < COALESCE(last_check_in_at, created_at)');
            })
            ->get();

        $count = 0;
        foreach ($usersToRemind as $user) {
            try {
                if ($user->isInQuietHours()) {
                    Log::info("Usuario {$user->id} ({$user->email}) está en horas silenciosas. Omitiendo recordatorio preventivo.");
                    continue;
                }

                $this->sendReminder($user);

                // Actualizar el timestamp del último recordatorio enviado
                $user->update([
                    'last_reminder_sent_at' => now(),
                ]);

                $count++;
                $this->info("Recordatorio enviado a: {$user->email}");
            } catch (Exception $e) {
                Log::error("Error al enviar recordatorio a usuario {$user->id}: ".$e->getMessage());
            }
        }

        $this->info("Proceso completado. Se enviaron {$count} recordatorios.");
    }

    protected function sendReminder(User $user)
    {
        $actionUrl = config('app.frontend_url', env('FRONTEND_URL', 'http://localhost:3000'));

        // 1. Enviar notificación Push a través de Expo
        if ($user->expo_push_token) {
            $response = Http::post('https://exp.host/--/api/v2/push/send', [
                'to' => $user->expo_push_token,
                'title' => '¿Estás por ahí?',
                'body' => 'Recuerda confirmar tu bienestar para evitar avisar a tus contactos.',
                'data' => ['type' => 'checkin_reminder'],
            ]);

            if ($response->failed()) {
                Log::warning("Expo Push Notification reminder failed for user {$user->id}: ".$response->body());
            } else {
                Log::info("Expo Push Notification reminder sent to user {$user->id}");
            }
        } else {
            Log::info("User {$user->id} does not have an expo_push_token registered. Skipping push.");
        }

        // 2. Enviar email de recordatorio
        Mail::to($user->email)->send(new CheckInReminderMail($user, $actionUrl));
        Log::info("Email reminder sent to user {$user->id} ({$user->email})");
    }
}
