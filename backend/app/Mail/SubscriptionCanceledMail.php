<?php

namespace App\Mail;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Attachment;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class SubscriptionCanceledMail extends Mailable
{
    use Queueable, SerializesModels;

    public User $user;
    public string $reason;
    public int $activePremiumUsers;

    /**
     * Create a new message instance.
     */
    public function __construct(User $user, string $reason = 'Cancelación voluntaria o expiración en Google Play', int $activePremiumUsers = 0)
    {
        $this->user = $user;
        $this->reason = $reason;
        $this->activePremiumUsers = $activePremiumUsers > 0 ? $activePremiumUsers : User::where('is_premium', true)->count();
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "⚠️ Suscripción Cancelada: {$this->user->name} - Estoy Ok",
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            markdown: 'emails.subscription-canceled',
        );
    }

    /**
     * Get the attachments for the message.
     *
     * @return array<int, Attachment>
     */
    public function attachments(): array
    {
        return [];
    }
}
