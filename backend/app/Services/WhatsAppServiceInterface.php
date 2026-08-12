<?php

namespace App\Services;

interface WhatsAppServiceInterface
{
    /**
     * Send a WhatsApp message.
     *
     * @param  string  $to  The recipient's phone number.
     * @param  string  $message  The message content or template name.
     * @param  array  $parameters  Optional parameters for templates.
     */
    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool;
}
