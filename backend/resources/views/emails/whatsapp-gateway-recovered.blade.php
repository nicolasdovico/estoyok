<x-mail::message>
# ✅ Servicio Restablecido: WhatsApp Operativo

El monitor automático **WhatsApp Guardian** de Estoy Ok informa que la pasarela de WhatsApp se ha **reconectado exitosamente** y el socket se encuentra en estado `open`.

### 📋 Detalle de la Restauración:
- **Estado Actual:** `OPEN (Conectado)`
- **Fecha y Hora de Recuperación:** {{ $restoredAt }}
@if($downtimeMinutes)
- **Tiempo aproximado fuera de línea:** {{ $downtimeMinutes }} minutos
@endif

Todos los envíos de alertas de SOS, check-ins y mensajes de prueba han vuelto a la normalidad operativa.

Atentamente,<br>
Sistema Guardián de {{ config('app.name') }}
</x-mail::message>
