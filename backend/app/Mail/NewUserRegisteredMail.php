<?php

namespace App\Mail;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Attachment;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class NewUserRegisteredMail extends Mailable
{
    use Queueable, SerializesModels;

    public User $user;
    public int $totalUsers;

    /**
     * Create a new message instance.
     */
    public function __construct(User $user, int $totalUsers = 0)
    {
        $this->user = $user;
        $this->totalUsers = $totalUsers > 0 ? $totalUsers : User::count();
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "🎉 Nuevo Usuario Registrado: {$this->user->name} - Estoy Ok",
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            markdown: 'emails.new-user-registered',
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
