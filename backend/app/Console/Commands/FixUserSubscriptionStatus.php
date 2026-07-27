<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;

class FixUserSubscriptionStatus extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'user:reset-free {email}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Reset a user to completely Free status in database';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $email = $this->argument('email');
        $user = User::where('email', $email)->first();

        if (! $user) {
            $this->error("Usuario con email {$email} no fue encontrado.");
            return 1;
        }

        $user->update([
            'is_premium' => false,
            'subscription_status' => 'inactive',
            'subscription_provider' => 'stripe',
            'subscription_id' => null,
            'trial_ends_at' => null,
            'billing_cycle_ends_at' => null,
            'trial_reminder_sent_at' => null,
            'grace_period_ends_at' => null,
            'mp_subscription_id' => null,
            'mp_status' => null,
            'paypal_subscription_id' => null,
            'paypal_status' => null,
        ]);

        if (method_exists($user, 'subscriptions')) {
            $user->subscriptions()->delete();
        }

        $this->info("El usuario {$email} se ha restablecido a modo GRATIS (Free) con éxito.");
        $this->line(json_encode([
            'id' => $user->id,
            'email' => $user->email,
            'is_premium' => $user->is_premium,
            'subscription_status' => $user->subscription_status,
            'has_premium_access' => $user->hasPremiumAccess(),
        ], JSON_PRETTY_PRINT));

        return 0;
    }
}
