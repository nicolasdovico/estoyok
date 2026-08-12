# Plan Integrador de Migración: Twilio / UltraMsg ➔ Evolution API (Self-Hosted WhatsApp Gateway)

## Objetivos del Plan
1. Integrar **Evolution API v2** (pasarela Open Source de WhatsApp basada en Docker) para lograr **costo $0/mes**, mensajes ilimitados y control total de la infraestructura sin depender de suscripciones de terceros.
2. Desplegar el contenedor de Evolution API en `docker-compose.yml` para desarrollo local y proveer las instrucciones de despliegue en **Railway**.
3. Implementar el cliente `EvolutionApiService.php` en Laravel 12 API conectado a `WhatsAppServiceInterface`.
4. Configurar el Webhook entrante para procesar los eventos `MESSAGES_UPSERT` (recepción de respuestas "OK" para Check-in Pasivo).
5. Vincular la línea telefónica mediante escaneo de Código QR desde el panel de administración o endpoints de la API de Evolution.

---

## 1. Configuración de Infraestructura (Docker Compose & Railway)

### 1.1 docker-compose.yml (Entorno Local)
Agregar el servicio `evolution-api` al archivo `docker-compose.yml`:

```yaml
  evolution-api:
    image: atendai/evolution-api:v2.1.1
    container_name: estoyok_evolution_api
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SERVER_URL=http://localhost:8080
      - API_KEY=estoyok_secret_key_2026
      - AUTHENTICATION_TYPE=apikey
      - DATABASE_ENABLED=false
    volumes:
      - evolution_instances:/evolution/instances

volumes:
  ...
  evolution_instances:
```

### 1.2 Configuración en Railway (Entorno de Producción)

1. En el proyecto de Railway (**Estoy Ok**), hacer clic en **+ New** ➔ **Docker Image**.
2. Ingresar el nombre de la imagen oficial: `evoapicloud/evolution-api:v2.2.0`.
3. Renombrar el servicio a `evolution-api`.
4. Asignar las variables de entorno en la pestaña **Variables** del servicio `evolution-api`:
   - `SERVER_URL`: `https://${RAILWAY_PUBLIC_DOMAIN}` (o la URL pública asignada por Railway).
   - `AUTHENTICATION_TYPE`: `apikey`
   - `AUTHENTICATION_API_KEY`: `estoyok_secret_key_prod_2026`
   - `API_KEY`: `estoyok_secret_key_prod_2026`
   - `DATABASE_ENABLED`: `true`
   - `DATABASE_PROVIDER`: `postgresql`
   - `DATABASE_CONNECTION_URI`: `${DATABASE_URL}?schema=evolution&connection_limit=20&pool_timeout=30`
   - `DATABASE_SAVE_DATA_INSTANCE`: `true`
   - `DATABASE_SAVE_DATA_NEW_MESSAGE`: `true`
   - `DATABASE_SAVE_DATA_CONTACTS`: `false`
   - `DATABASE_SAVE_DATA_CHATS`: `false`
   - `DATABASE_SAVE_DATA_HISTORIC`: `false`
   - `CACHE_REDIS_ENABLED`: `true`
   - `CACHE_REDIS_URI`: `${REDIS_URL}` (reutiliza el Redis del proyecto en Railway).
   - `CACHE_REDIS_PREFIX_KEY`: `evolution`
   - `CACHE_REDIS_SAVE_INSTANCES`: `true`
   - `CONFIG_SESSION_PHONE_CLIENT`: `Chrome`
   - `CONFIG_SESSION_PHONE_NAME`: `Chrome`
   - `CONFIG_SESSION_PHONE_VERSION`: `2.3000.1043857760`
   - `CONFIG_SESSION_PHONE_SYNC_FULL_HISTORY`: `false`
   - `NODE_OPTIONS`: `--network-family-autoselection-attempt-timeout=1000`
   - `PORT`: `8080`

5. En **Settings ➔ Networking** del servicio `evolution-api`, presionar **Generate Domain** (ej: `https://evolution-api-production-xxxx.up.railway.app`).
6. En el servicio `backend` de Laravel en Railway, actualizar las variables de entorno:
   - `EVOLUTION_API_URL`: `https://evolution-api-production-xxxx.up.railway.app`
   - `EVOLUTION_API_KEY`: `estoyok_secret_key_prod_2026`
   - `EVOLUTION_INSTANCE_NAME`: `estoyok_main`

---

## 2. Modificaciones en la Arquitectura Backend (Laravel 12 API)

### 2.1 Variables de Entorno y Configuración (`.env`, `services.php`)
- **`.env` y `.env.example`**:
  ```env
  # Evolution API (WhatsApp Gateway Self-Hosted)
  EVOLUTION_API_URL=http://evolution-api:8080
  EVOLUTION_API_KEY=estoyok_secret_key_2026
  EVOLUTION_INSTANCE_NAME=estoyok_main
  ```

- **`config/services.php`**:
  ```php
  'evolution' => [
      'url' => env('EVOLUTION_API_URL', 'http://evolution-api:8080'),
      'api_key' => env('EVOLUTION_API_KEY'),
      'instance' => env('EVOLUTION_INSTANCE_NAME', 'estoyok_main'),
  ],
  ```

### 2.2 Creación del Servicio `EvolutionApiService.php`
Crear `app/Services/EvolutionApiService.php` implementando `WhatsAppServiceInterface`:

```php
namespace App\Services;

use Exception;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class EvolutionApiService implements WhatsAppServiceInterface
{
    protected ?string $baseUrl;
    protected ?string $apiKey;
    protected ?string $instance;

    public function __construct()
    {
        $this->baseUrl = rtrim(config('services.evolution.url'), '/');
        $this->apiKey = config('services.evolution.api_key');
        $this->instance = config('services.evolution.instance', 'estoyok_main');
    }

    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool
    {
        if (! $this->baseUrl || ! $this->apiKey) {
            Log::info("[SIMULATED EVOLUTION WHATSAPP] To: {$to} | Message: {$message}");
            return true;
        }

        try {
            // Limpiar teléfono a dígitos en formato internacional
            $cleanTo = preg_replace('/[^0-9]/', '', $to);

            $url = "{$this->baseUrl}/message/sendText/{$this->instance}";

            $response = Http::withHeaders([
                'apikey' => $this->apiKey,
                'Content-Type' => 'application/json',
            ])->post($url, [
                'number' => $cleanTo,
                'text' => $message,
            ]);

            if ($response->successful()) {
                return true;
            }

            Log::error('Evolution API Error: ' . $response->body());
            return false;
        } catch (Exception $e) {
            Log::error('Evolution API Exception: ' . $e->getMessage());
            return false;
        }
    }
}
```

### 2.3 Binding en `AppServiceProvider.php`
```php
use App\Services\EvolutionApiService;
use App\Services\WhatsAppServiceInterface;

// En register():
$this->app->singleton(WhatsAppServiceInterface::class, EvolutionApiService::class);
```

### 2.4 Procesamiento de Webhook Entrante (`WebhookController.php` & `routes/api.php`)
- **`WebhookController.php` (`evolutionMessage`)**:
  - Parsear el payload JSON enviado por Evolution API en eventos `MESSAGES_UPSERT`:
    ```json
    {
      "event": "messages.upsert",
      "instance": "estoyok_main",
      "data": {
        "key": {
          "remoteJid": "5491123456789@s.whatsapp.net",
          "fromMe": false
        },
        "message": {
          "conversation": "OK"
        }
      }
    }
    ```
  - Extraer `$remoteJid`, limpiar la extensión `@s.whatsapp.net` o `@g.us` (omitir mensajes de grupos).
  - Buscar usuario en la BD y si el cuerpo del mensaje es "OK", invocar el check-in con origen `'whatsapp'`.

- **`routes/api.php`**:
  - Registrar la ruta: `Route::post('/webhooks/evolution/message', [WebhookController::class, 'evolutionMessage']);`.
  - Actualizar ruta de prueba: `POST /api/maintenance/send-test-whatsapp` llamando a `EvolutionApiService`.

### 2.5 Pruebas Automatizadas (`EvolutionWebhookTest.php`)
- Crear `tests/Feature/EvolutionWebhookTest.php` para testear el manejo del webhook de Evolution API.
- Correr `./test.sh` y garantizar el 100% de pasaje (141/141 tests PASS).

---

## 3. Guía Paso a Paso para la Vinculación por QR (Puesta en Marcha)

### Paso 1: Crear la Instancia en Evolution API
Mediante una petición HTTP POST desde Postman, cURL o script de mantenimiento:
```bash
curl -X POST "http://localhost:8080/instance/create" \
  -H "apikey: estoyok_secret_key_2026" \
  -H "Content-Type: application/json" \
  -d '{
    "instanceName": "estoyok_main",
    "qrcode": true,
    "integration": "WHATSAPP-BAILEYS"
  }'
```

### Paso 2: Conectar y Obtener el Código QR
Obtener el QR base64 o abrir en el navegador:
`GET http://localhost:8080/instance/connect/estoyok_main` (con header `apikey: estoyok_secret_key_2026`).

Escanear el código QR desde la app de WhatsApp del celular dedicado a Estoy Ok (**Dispositivos vinculados ➔ Vincular dispositivo**).

### Paso 3: Registrar el Webhook Automático en Evolution API
Enviar petición HTTP POST a Evolution para apuntar a nuestro backend:
```bash
curl -X POST "http://localhost:8080/webhook/set/estoyok_main" \
  -H "apikey: estoyok_secret_key_2026" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "url": "https://api.estoyok24.com/api/webhooks/evolution/message",
    "byEvents": false,
    "events": ["MESSAGES_UPSERT"]
  }'
```

---

## 4. Matriz de Archivos a Modificar / Crear

| Acción | Archivo / Componente | Descripción |
| :--- | :--- | :--- |
| ✨ **Crear** | `backend/app/Services/EvolutionApiService.php` | Cliente de envío de mensajes vía Evolution API v2 |
| ✨ **Crear** | `backend/tests/Feature/EvolutionWebhookTest.php` | Test de integración para webhook de Evolution API |
| ✏️ **Modificar** | `docker-compose.yml` | Agregado el contenedor de `evolution-api` v2 |
| ✏️ **Modificar** | `backend/app/Providers/AppServiceProvider.php` | Binding de `EvolutionApiService` |
| ✏️ **Modificar** | `backend/config/services.php` | Configuración de `'evolution'` (`url`, `api_key`, `instance`) |
| ✏️ **Modificar** | `backend/.env` y `.env.example` | Variables `EVOLUTION_API_URL`, `EVOLUTION_API_KEY`, `EVOLUTION_INSTANCE_NAME` |
| ✏️ **Modificar** | `backend/app/Http/Controllers/Api/WebhookController.php` | Método `evolutionMessage` para procesar respuestas |
| ✏️ **Modificar** | `backend/routes/api.php` | Ruta de webhook `/webhooks/evolution/message` |
