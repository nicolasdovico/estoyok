<x-mail::message>
# Recordatorio de Estoy Ok

Hola, **{{ $user->name }}**.

Este es un aviso automático de que estás cerca de alcanzar tu límite de tiempo de inactividad de **{{ $user->checkin_interval_hours }}** {{ $user->checkin_interval_hours == 1 ? 'hora' : 'horas' }} en tu reporte de bienestar.

Por favor, confirma que te encuentras bien ingresando a la aplicación o haciendo clic en el siguiente botón para evitar que se disparen alertas automáticas a tus contactos de emergencia:

<x-mail::button :url="$actionUrl">
Confirmar Bienestar (Estoy OK)
</x-mail::button>

Si ya has realizado tu reporte o no tienes acceso a la app en este momento, puedes ingresar al enlace anterior para confirmar tu estado de forma segura.

Gracias por confiar en nosotros,<br>
El equipo de {{ config("app.name") }}
</x-mail::message>
