<?php

namespace App\Providers;

use App\Services\UltraMsgService;
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
        $this->app->singleton(WhatsAppServiceInterface::class, UltraMsgService::class);
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        $alwaysTo = config('mail.always_to') ?? env('MAIL_ALWAYS_TO');
        if (!empty($alwaysTo)) {
            Mail::alwaysTo($alwaysTo);
        }
    }
}
