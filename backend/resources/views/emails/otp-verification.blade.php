<x-mail::message>
# Verificación de Email

Hola,

Gracias por registrarte en **Estoy Ok**. Para completar tu registro y verificar tu cuenta, utiliza el siguiente código de 6 dígitos:

<x-mail::panel>
## {{ $code }}
</x-mail::panel>

Este código expirará en 15 minutos.

Si no solicitaste este código, puedes ignorar este mensaje.

Gracias,<br>
{{ config('app.name') }}
</x-mail::message>
