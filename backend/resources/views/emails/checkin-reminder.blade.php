<x-mail::message>
# Recordatorio de Estoy Ok

Hola, **{{ $user->name }}**.

Este es un aviso automático de que estás cerca de alcanzar tu límite de tiempo de inactividad de **{{ $user->checkin_interval_hours }}** {{ $user->checkin_interval_hours == 1 ? 'hora' : 'horas' }} en tu reporte de bienestar.

Por favor, abre la aplicación móvil **Estoy Ok** en tu teléfono y confirma tu bienestar para evitar que se disparen alertas automáticas a tus contactos de emergencia.

Gracias por confiar en nosotros,<br>
{{ config("app.name") }}
</x-mail::message>

