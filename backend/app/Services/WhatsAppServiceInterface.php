<?php

namespace App\Services;

interface WhatsAppServiceInterface
{
    /**
     * Send a WhatsApp message.
     *
     * @param string $to The recipient's phone number.
     * @param string $message The message content or template name.
     * @param array $parameters Optional parameters for templates.
     * @return bool
     */
    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool;

    /**
     * Send an SMS message (fallback).
     *
     * @param string $to The recipient's phone number.
     * @param string $message The message content.
     * @return bool
     */
    public function sendSMS(string $to, string $message): bool;
}
