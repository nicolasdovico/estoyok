<x-mail::message>
# 🚨 Alerta de Infraestructura: WhatsApp Desconectado

El monitor automático **WhatsApp Guardian** de Estoy Ok ha detectado que la pasarela de WhatsApp (Evolution API / Baileys) se encuentra **fuera de servicio** o con la sesión desconectada.

### 📋 Detalle del Estado:
- **Estado Detectado:** `{{ strtoupper($status) }}`
- **Fecha y Hora:** {{ $detectedAt }}
- **Impacto:** Los mensajes automáticos de SOS, alertas de check-in, bienvenida y notificaciones a contactos de emergencia están interrumpidos o derivando a canales secundarios.

@if(!empty($errorMessage))
> **Mensaje del sistema:** {{ $errorMessage }}
@endif

### 🔧 Acción Requerida:
Por favor, ingresa al panel de reconexión para re-vincular o escanear el código QR de la instancia:

<x-mail::button :url="$connectQrUrl" color="error">
Escanear QR / Reconectar WhatsApp
</x-mail::button>

*Este aviso se envía automáticamente a administración con una ventana de silencio de 3 horas para evitar repeticiones.*

Atentamente,<br>
Sistema Guardián de {{ config('app.name') }}
</x-mail::message>
