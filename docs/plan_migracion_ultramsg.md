# Plan Integrador de Migración: Twilio ➔ UltraMsg (WhatsApp Gateway)

## Objetivos del Plan
1. Sustituir por completo la pasarela **Twilio** por **UltraMsg** para el despacho automatizado de notificaciones de WhatsApp (Alertas de Inactividad, Alertas de Colisión/Crash, Notificaciones de Emergencia y pruebas del sistema).
2. Remover formalmente el envío de mensajes **SMS desde el servidor** (ya que UltraMsg se enfoca exclusivamente en la API de WhatsApp), simplificando la arquitectura a alertas en tiempo real vía WhatsApp y Push.
3. Configurar e integrar el Webhook de UltraMsg para recepción de mensajes entrantes (Auto Check-in respondiendo "OK" por WhatsApp).
4. Proveer la guía paso a paso para el registro, vinculación de dispositivo por QR y configuración del panel de UltraMsg.

---

## 1. Registro y Configuración Externa en UltraMsg

### Paso 1.1: Creación de Cuenta e Instancia
1. Registrarse en el sitio oficial de **UltraMsg** ([ultramsg.com](https://ultramsg.com)).
2. En la consola principal (*Dashboard*), crear una nueva **Instancia de WhatsApp**.
3. Copiar y resguardar las credenciales proporcionadas:
   - **`INSTANCE_ID`**: Identificador único de la instancia (ejemplo: `instance104892`).
   - **`TOKEN`**: Token de autenticación de la API (ejemplo: `9x8a7b6c5d4e3f2a`).

### Paso 1.2: Vinculación del Dispositivo (QR Code)
1. En el panel de la instancia en UltraMsg, hacer clic en **"Show QR Code"**.
2. Abrir WhatsApp (o WhatsApp Business) en el dispositivo celular destinado a emitir las notificaciones oficiales de Estoy Ok.
3. Ir a **Ajustes / Menú ➔ Dispositivos vinculados ➔ Vincular un dispositivo** y escanear el código QR presentado en la pantalla de UltraMsg.
4. Confirmar que el estado de la instancia cambie a **`Authenticated / Connected`**.

### Paso 1.3: Configuración del Webhook Entrante (Check-in Pasivo/Manual por WhatsApp)
1. En el menú lateral de la instancia en UltraMsg, seleccionar **Webhook Settings**.
2. Configurar las propiedades:
   - **Webhook URL:** `https://api.estoyok24.com/api/webhooks/ultramsg/message`
   - **Webhook Status:** `Enable` (Activo).
   - **Events to send:** Seleccionar `onMessage` (dispara la petición HTTP al recibir un mensaje).
3. Guardar cambios.

---

## 2. Modificaciones en la Arquitectura Backend (Laravel 12 API)

### 2.1 Limpieza de Paquetes y Entorno (`composer.json`, `.env`, `services.php`)
- **Remoción de Twilio SDK:**
  Ejecutar `composer remove twilio/sdk` en la carpeta `backend/`.
- **Variables de Entorno (`.env` & `.env.example`):**
  - Eliminar: `TWILIO_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_WHATSAPP_FROM`, `TWILIO_SMS_FROM`.
  - Incorporar:
    ```env
    ULTRAMSG_INSTANCE_ID=instance104892
    ULTRAMSG_TOKEN=tu_token_de_ultramsg_aqui
    ```
- **Configuración de Servicios (`config/services.php`):**
  Reemplazar el bloque `'twilio'` por:
  ```php
  'ultramsg' => [
      'instance_id' => env('ULTRAMSG_INSTANCE_ID'),
      'token' => env('ULTRAMSG_TOKEN'),
  ],
  ```

### 2.2 Actualización de Contrato de Servicio (`WhatsAppServiceInterface.php`)
Remover la firma `sendSMS` para dejar únicamente la interfaz enfocada en mensajes de WhatsApp:
```php
namespace App\Services;

interface WhatsAppServiceInterface
{
    /**
     * Enviar un mensaje de WhatsApp vía UltraMsg API.
     */
    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool;
}
```

### 2.3 Creación de `UltraMsgService.php` y Eliminación de `TwilioService.php`
- Eliminar el archivo `app/Services/TwilioService.php`.
- Crear el nuevo servicio `app/Services/UltraMsgService.php` implementando `WhatsAppServiceInterface`:
  - Utiliza `Illuminate\Support\Facades\Http` para realizar peticiones HTTP POST a `https://api.ultramsg.com/{instance_id}/messages/chat`.
  - Normaliza los números telefónicos en formato E.164 (eliminando símbolos especiales, espacios o el prefijo `whatsapp:` que usaba Twilio).
  - Maneja la simulación en entorno local cuando no existan credenciales configuradas (`Log::info("[SIMULATED ULTRAMSG WHATSAPP]...")`).

```php
namespace App\Services;

use Exception;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class UltraMsgService implements WhatsAppServiceInterface
{
    protected ?string $instanceId;
    protected ?string $token;

    public function __construct()
    {
        $this->instanceId = config('services.ultramsg.instance_id');
        $this->token = config('services.ultramsg.token');
    }

    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool
    {
        if (! $this->instanceId || ! $this->token) {
            Log::info("[SIMULATED ULTRAMSG WHATSAPP] To: {$to} | Message: {$message}");
            return true;
        }

        try {
            // Normalizar teléfono a dígitos con código de país
            $cleanTo = preg_replace('/[^0-9]/', '', $to);

            $response = Http::asForm()->post("https://api.ultramsg.com/{$this->instanceId}/messages/chat", [
                'token' => $this->token,
                'to' => $cleanTo,
                'body' => $message,
                'priority' => 10,
            ]);

            if ($response->successful()) {
                return true;
            }

            Log::error('UltraMsg WhatsApp API Error: ' . $response->body());
            return false;
        } catch (Exception $e) {
            Log::error('UltraMsg Exception: ' . $e->getMessage());
            return false;
        }
    }
}
```

### 2.4 Inyección de Dependencias (`AppServiceProvider.php`)
Actualizar el binding en `app/Providers/AppServiceProvider.php`:
```php
use App\Services\UltraMsgService;
use App\Services\WhatsAppServiceInterface;

// En el método register():
$this->app->singleton(WhatsAppServiceInterface::class, UltraMsgService::class);
```

### 2.5 Depuración de Jobs y Controladores (Eliminación de Fallbacks SMS)
- **`app/Jobs/SendInactivityAlerts.php`**:
  - Eliminar el bloque de fallback a `$whatsAppService->sendSMS(...)`.
  - Despachar directamente `$whatsAppService->sendWhatsApp(...)` y registrar logs si retorna `false`.
- **`app/Jobs/SendCrashAlertJob.php`**:
  - Eliminar la llamada a SMS del servidor Twilio, manteniendo únicamente el despacho de WhatsApp.
- **`app/Http/Controllers/EmergencyAlertController.php`**:
  - Ajustar para invocar únicamente la alerta por WhatsApp.

### 2.6 Procesamiento de Webhooks Entrantes (`WebhookController.php` & `routes/api.php`)
- **`WebhookController.php`**:
  Reemplazar el método `twilioMessage` por `ultramsgMessage(Request $request)`:
  - Formato de carga útil entrante de UltraMsg:
    ```json
    {
      "event_type": "message_received",
      "instanceId": "instance104892",
      "data": {
        "id": "...",
        "from": "5491123456789@c.us",
        "to": "...",
        "body": "OK",
        "type": "chat"
      }
    }
    ```
  - Parsear `$request->input('data.from')` extrayendo el número telefónico sin `@c.us`.
  - Parsear el mensaje (`data.body`). Si coincide con respuestas de bienestar ("OK", "ESTOY OK", "1"), invocar el controlador de Check-in marcando origen `'whatsapp'`.
- **`routes/api.php`**:
  - Reemplazar `Route::post('/webhooks/twilio/message', ...)` por:
    `Route::post('/webhooks/ultramsg/message', [WebhookController::class, 'ultramsgMessage']);`
  - Reemplazar la ruta de mantenimiento `POST /api/maintenance/send-test-sms` por `POST /api/maintenance/send-test-whatsapp` utilizando la nueva pasarela UltraMsg.

### 2.7 Pruebas Automatizadas y Regeneración de Swagger
- Actualizar/Renombrar `tests/Feature/TwilioWebhookTest.php` a `tests/Feature/UltraMsgWebhookTest.php` probando la recepción del JSON payload de UltraMsg.
- Actualizar `tests/Feature/WhatsAppAlertTest.php`, `tests/Feature/CrashAlertTest.php` e `tests/Feature/InactivityEscalationTest.php` para validar los envíos con la interfaz actualizada.
- Ejecutar `./test.sh` y verificar que **142/142 tests pasen al 100%**.
- Regenerar especificación Swagger: `docker compose exec backend php artisan l5-swagger:generate`.

---

## 3. Actualización en Clientes (Frontend Web & Android Nativo)

### 3.1 Frontend Web (Next.js)
- Actualizar componentes con referencias de texto a Twilio/SMS:
  - `frontend-web/src/components/SecuritySettings.tsx`
  - `frontend-web/src/components/BillingSection.tsx`
  - `frontend-web/src/app/page.tsx` (Landing page)
- Reemplazar menciones por **"Alertas por WhatsApp (UltraMsg)"**.

### 3.2 App Móvil Nativa (Android Kotlin)
- Renombrar etiquetas y textos informativos:
  - `AjustesScreen.kt`: Renombrar tarjeta de `"Reporte por SMS / WhatsApp"` a `"Reporte por WhatsApp"`.
  - `AjustesViewModel.kt`: Actualizar mensajes de estado a `"Ajustes de WhatsApp actualizados."`.
  - `PremiumScreen.kt`: Actualizar viñetas de características a *"Alertas por WhatsApp ilimitadas"*.
- **Aclaración sobre SMS local en Android Nativo (`SmsManager`):**
  La funcionalidad de *SMS de Emergencia Offline* ejecutada directamente por el hardware del chip celular en `CrashAlertActivity.kt` y `PanelViewModel.kt` se mantiene intacta como respaldo sin conexión a internet cuando el dispositivo no tiene datos móviles. Lo que se elimina es la dependencia de SMS vía servidor Twilio.

---

## 4. Matriz de Archivos a Modificar / Eliminar / Crear

| Acción | Archivo / Componente | Descripción |
| :--- | :--- | :--- |
| ❌ **Eliminar** | `backend/app/Services/TwilioService.php` | Servicio legacy de Twilio SDK |
| ❌ **Eliminar** | `backend/tests/Feature/TwilioWebhookTest.php` | Test legacy de webhook Twilio |
| ✨ **Crear** | `backend/app/Services/UltraMsgService.php` | Implementación HTTP cliente UltraMsg API |
| ✨ **Crear** | `backend/tests/Feature/UltraMsgWebhookTest.php` | Pruebas automatizadas de webhook UltraMsg |
| ✏️ **Modificar** | `backend/app/Services/WhatsAppServiceInterface.php` | Eliminado método `sendSMS` de la interfaz |
| ✏️ **Modificar** | `backend/app/Providers/AppServiceProvider.php` | Binding de `UltraMsgService` |
| ✏️ **Modificar** | `backend/config/services.php` | Reemplazada sección `'twilio'` por `'ultramsg'` |
| ✏️ **Modificar** | `backend/.env` y `.env.example` | Variables `ULTRAMSG_INSTANCE_ID` y `ULTRAMSG_TOKEN` |
| ✏️ **Modificar** | `backend/app/Jobs/SendInactivityAlerts.php` | Eliminado fallback SMS de servidor |
| ✏️ **Modificar** | `backend/app/Jobs/SendCrashAlertJob.php` | Eliminado fallback SMS de servidor |
| ✏️ **Modificar** | `backend/app/Http/Controllers/Api/WebhookController.php` | Nuevo procesador de webhook UltraMsg |
| ✏️ **Modificar** | `backend/routes/api.php` | Actualización de webhook y endpoint de mantenimiento |
| ✏️ **Modificar** | `android-native/.../AjustesScreen.kt` | Actualización de labels e interfaz UI |
| ✏️ **Modificar** | `frontend-web/.../SecuritySettings.tsx` | Actualización de textos en plataforma Web |

---

## 5. Plan de Ejecución y Validación en Producción (Railway)

1. **Variables de Entorno:** Cargar `ULTRAMSG_INSTANCE_ID` y `ULTRAMSG_TOKEN` en Railway Dashboard y remover variables de Twilio.
2. **Despliegue Backend:** Commit convencional `feat(whatsapp): migrar pasarela de WhatsApp de Twilio a UltraMsg` y push a repositorio.
3. **Prueba de Transmisión Real:** Invocar `POST https://api.estoyok24.com/api/maintenance/send-test-whatsapp` para verificar envío real a celular.
4. **Prueba de Respuesta Entrante:** Responder "ESTOY OK" desde WhatsApp al número emisor y verificar la actualización automática del reporte de bienestar en la base de datos.
