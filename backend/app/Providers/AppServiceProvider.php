<?php

namespace App\Providers;

use App\Services\TwilioService;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Mail;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        $this->app->singleton(WhatsAppServiceInterface::class, TwilioService::class);
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        if (env('MAIL_ALWAYS_TO')) {
            Mail::alwaysTo(env('MAIL_ALWAYS_TO'));
        }
    }
}
