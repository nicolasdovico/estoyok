<?php

namespace App\Jobs;

use App\Services\WhatsAppServiceInterface;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Log;

class SendCheckinConfirmationWhatsAppJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public string $phone;
    public ?int $userId;

    /**
     * Create a new job instance.
     */
    public function __construct(string $phone, ?int $userId = null)
    {
        $this->phone = $phone;
        $this->userId = $userId;
    }

    /**
     * Execute the job.
     */
    public function handle(WhatsAppServiceInterface $whatsAppService): void
    {
        try {
            $whatsAppService->sendWhatsApp(
                $this->phone,
                '✅ Bienestar verificado con éxito en Estoy Ok. ¡Gracias!'
            );
            Log::info("SendCheckinConfirmationWhatsAppJob: Confirmation sent to {$this->phone} for user " . ($this->userId ?? 'null'));
        } catch (\Throwable $e) {
            Log::warning("SendCheckinConfirmationWhatsAppJob: Failed to send to {$this->phone}: " . $e->getMessage());
        }
    }
}
