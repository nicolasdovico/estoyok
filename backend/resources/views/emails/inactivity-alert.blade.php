<x-mail::message>
# Alerta de Seguridad de Estoy Ok

Este es un aviso automático porque su contacto de emergencia, **{{ $user->name }}**, no ha realizado su reporte de bienestar diario en el tiempo configurado.

@if($relationship)
*(Usted figura como **{{ $relationship }}** en su lista de contactos)*
@endif

Para su tranquilidad, puede ver la **última ubicación conocida** y el estado de la alerta en el siguiente enlace:

<x-mail::button :url="$emergencyUrl">
Ver Ubicación en el Mapa
</x-mail::button>

Si logra comunicarse con {{ $user->name }} y confirma que todo está bien, puede ignorar este mensaje. La alerta se desactivará automáticamente cuando el usuario realice su check-in en la aplicación.

Gracias por usar,<br>
{{ config("app.name") }}
</x-mail::message>
