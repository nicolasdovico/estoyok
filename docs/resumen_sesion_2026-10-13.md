# Resumen de Sesión - 13 de Octubre de 2026
**Objetivo principal:** Estabilización, aislamiento y verificación integral de las alertas de inactividad de bienestar por WhatsApp (reloj en 0) tanto en **Producción** (`api.estoyok24.com`) como en **Dev** (`backend-api-dev-2a56.up.railway.app`).

---

## 1. Resumen Ejecutivo
Durante la sesión de hoy se resolvieron 4 problemas arquitectónicos críticos que causaban fallas intermitentes, aparente "rotura cruzada" entre Dev y Producción, cancelación silenciosa de cuentas Premium y desconexiones de WhatsApp en Evolution API:

1. **Colisión de colas en Redis (Dev vs Producción):** Ambos entornos compartían la misma instancia de Redis (`redis.railway.internal`) con idéntico prefijo de claves (`estoy-ok-database-`) y la cola `default`. El worker de Producción robaba los jobs encolados por Dev; al buscar el UUID de la alerta en la base de datos de Producción (`estoyok_db`), no existía y la descartaba silenciosamente.
2. **Secuestro de Webhook en Evolution API:** Dev y Producción compartían la misma instancia de WhatsApp (`estoyok_main`). Al despachar un mensaje, Dev reconfiguraba el webhook hacia su propia URL, dejando a Producción sin capacidad de recibir respuestas "OK"; y viceversa.
3. **Falla de socket Baileys Error 1006 (Bad Request 400):** Evolution API arrojaba `{"status":400,"response":{"message":["1006"]}}` cuando el socket WebSocket se cerraba. El código anterior solo buscaba la cadena `"Connection Closed"`, por lo que no disparaba la auto-reconexión en caliente y el mensaje se perdía.
4. **Revocación silenciosa de Premium en Android:** Al reanudar la app (`onResume`), `MainActivity.kt` invocaba `syncGooglePlay`. Al no encontrar una compra en Play Console, el backend revocaba el estado Premium de pruebas (`admin_test`).
5. **Bloqueo de sesión Signal/Double Ratchet en WhatsApp:** El envío consecutivo e inmediato del auto-aviso y del mensaje al contacto de emergencia (cuando ambos compartían el mismo número de teléfono) bloqueaba la sesión en Evolution API.

---

## 2. Entornos y Cuentas de Prueba

### Producción
- **Dominio API:** `https://api.estoyok24.com`
- **Base de Datos PostgreSQL:** `estoyok_db`
- **Prefijo Redis aislado:** `estoy-ok_production-database-`
- **Cola Redis:** `default`
- **Usuario de prueba:** `nicolas@gmail.com`
- **Estado:** **100% Funcional y verificado.** Vence el reloj naturalmente, el planificador encola la alerta, el worker despacha el auto-aviso y el aviso al contacto con el enlace al mapa de rescate, y la respuesta "OK" por WhatsApp renueva el reloj a 12 horas.

### Dev (Staging Railway)
- **Dominio API:** `https://backend-api-dev-2a56.up.railway.app`
- **Base de Datos PostgreSQL:** `railway`
- **Prefijo Redis aislado:** `estoy-ok_dev-database-`
- **Cola Redis:** `default` (worker escuchando `default,dev_default`)
- **Usuario de prueba:** `tamarakshop@gmail.com` (ID 45 en DB `railway`)
  - **Teléfono usuario:** `+5491149790220`
  - **Contacto de emergencia:** `Nico Sos` (ID 17, `+541149790220`)
- **Instancia WhatsApp:** `https://evolution-api-production-3449.up.railway.app` (`estoyok_main`)

---

## 3. Modificaciones y Soluciones Implementadas

### A. Aislamiento estricto de Redis entre Ambientes
- **Archivo:** `backend/config/database.php`
  - Se modificó la generación del prefijo de Redis:
    ```php
    'prefix' => env('REDIS_PREFIX', Str::slug((string) env('APP_NAME', 'estoy-ok')).'_'.(env('RAILWAY_ENVIRONMENT_NAME') ?: env('APP_ENV', 'production')).'-database-'),
    ```
  - En Railway Dev: `estoy-ok_dev-database-`
  - En Railway Producción: `estoy-ok_production-database-`
  - **Resultado:** Ningún worker de Producción puede leer ni vaciar claves o colas de Dev, y viceversa.
- **Archivo:** `backend/start.sh`
  - El supervisor del worker ahora escucha explícitamente:
    ```bash
    php artisan queue:work --queue="${REDIS_QUEUE:-default},dev_default" --sleep=3 --tries=3 --timeout=90 || true
    ```

### B. Protección de Webhooks en Evolution API
- **Archivo:** `backend/app/Services/EvolutionApiService.php`
  - En `ensureWebhookConfigured()`: Si el entorno es `dev` y la instancia es `estoyok_main`, se omite sobreescribir el webhook principal de Producción.
  - Producción mantiene el control del webhook oficial y puede espejar eventos a Dev mediante `EVOLUTION_WEBHOOK_PEER_URL` con header `X-EstoyOk-Mirrored: 1` (`WebhookController.php`).

### C. Auto-recuperación en caliente ante Error 1006 / Socket Cerrado
- **Archivo:** `backend/app/Services/EvolutionApiService.php`
  - Se expandió la detección de caída de socket para capturar `1006`, `close`, `is not open` y `Connection Closed`:
    ```php
    if (
        str_contains($responseBody, 'Connection Closed') ||
        str_contains($responseBody, '1006') ||
        str_contains($responseBody, 'is not open') ||
        str_contains($responseBody, 'Connecting') ||
        str_contains($responseBody, 'close')
    ) {
        $this->reconnectInstance();
        usleep(3000000); // 3s para asentar socket
        $retryResponse = Http::post($url, $payload);
        // ...
    }
    ```
  - **Resultado comprobado en vivo:** Al enviar al contacto `+541149790220`, Evolution API devolvió 1006; el servicio ejecutó la auto-reconexión inmediata y el reintento devolvió HTTP `201 Created`, entregando el mensaje en WhatsApp.

### D. Protección de Suscripciones Premium en Android y Backend
- **Archivo:** `android-native/app/src/main/java/com/estoyok/app/MainActivity.kt`
  - `onResume()` solo invoca la sincronización si `activePurchase != null`, evitando solicitudes de cancelación involuntarias al minimizar y abrir la app.
- **Archivo:** `backend/app/Http/Controllers/Api/SubscriptionController.php`
  - `syncGooglePlay` ahora respeta si el usuario tiene `trial_ends_at` vigente o si su proveedor es de pruebas (`admin_test`) o alternativo (`mercadopago`), sin degradarlo a Free.

### E. Optimización de Concurrencia y Delays en Jobs
- **Archivo:** `backend/app/Console/Commands/VerifyInactivity.php`
  - Bloqueo atómico de 120 segundos mediante `Cache::add("inactivity_alert_dispatched_user_{$user->id}", true, 120)`.
  - Creación atómica de `EmergencyAlert` en la base de datos antes de encolar `SendInactivityAlerts`, evitando duplicados por solapamiento de minutos.
- **Archivo:** `backend/app/Jobs/SendInactivityAlerts.php`
  - WhatsApp enviado con prioridad antes que los correos electrónicos.
  - Espaciado de 1.5s entre contactos distintos.
  - Espaciado de 3.5s si el número del contacto coincide con el número del propio usuario (para permitir que la sesión Signal de WhatsApp asiente el ratchet criptográfico entre el auto-aviso y el aviso de rescate).

### F. Telemetría y Diagnóstico en Tiempo Real
- **Ruta:** `GET /api/maintenance/diagnose-inactivity`
  - Informa: `redis_prefix`, `redis_queues` (`default_len`, `dev_default_len`, `keys`), `evolution_debug` (instancias y webhook), `last_inactivity_job_received`, `last_inactivity_job_self_whatsapp`, `last_inactivity_job_contacts_result`, `last_evolution_success`, `last_evolution_error`.
- **Ruta:** `GET|POST /api/maintenance/test-job-dispatch`
  - Inserta un job en Redis, mide la longitud de la cola inmediatamente, aguarda 2.5s y verifica el consumo del worker y los resultados en caché.

---

## 4. Estado de los Tests Automatizados
- **Total tests:** `190 passed (640 assertions)`
- **Duración:** ~20 segundos
- **Comando:** `docker compose exec backend php artisan test`
- **Regresiones:** Cero regresiones detectadas.

---

## 5. Puntos de Partida para la Sesión de Mañana
1. **Comportamiento en Dev entre `schedule:work` y `queue:work`:**
   - La prueba con `test-job-dispatch` en Dev funcionó de manera impecable: el job ingresó a Redis, el worker PID 57 lo tomó y despachó tanto el auto-aviso (`success: true`) como el aviso al contacto (`success: true`).
   - Sin embargo, en el disparo natural del reloj a las 21:12:00, `schedule:work` (PID 12) creó la alerta `01a137c0-6eca` en la base de datos `railway`, pero el job no actualizó la caché del worker. Mañana se debe revisar por qué el subproceso de `schedule:work` en Railway Dev no dejó trazas en el worker (revisar si `schedule:work` ejecuta en un contexto de base de datos o cola desfasada, o si requiere reiniciar los workers tras la creación de alerta).
2. **Verificación final en Dev con la app móvil instalada:**
   - Validar que al expirar el reloj en Dev, la notificación push llegue al teléfono de Tamara (`tamarakshop@gmail.com`) y que ambos mensajes de WhatsApp entren fluidamente.
3. **Limpieza de endpoints temporales de diagnóstico:**
   - Una vez consolidada la prueba final, retirar o asegurar con middleware de autenticación los endpoints auxiliares de `/maintenance/*`.
