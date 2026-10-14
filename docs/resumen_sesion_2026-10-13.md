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
- **Estado:** **100% Funcional y verificado en vivo.** Reloj preparado para expirar a las 21:58:10; `schedule:work` corrió a las 21:59:00 UTC, creó la alerta `01a137eb-75f6-7066-b125-986b7dedba9e`, el worker PID 34 tomó el job inmediatamente en el mismo segundo, despachó el auto-aviso a las 21:59:03 (`success: true`), activó la auto-recuperación de socket y entregó la alerta al contacto a las 21:59:10 (`success: true`, HTTP 201).

---

## 3. Modificaciones y Soluciones Implementadas

### A. Aislamiento estricto de Redis entre Ambientes
- **Archivo:** `backend/config/database.php`
  - Prefijo dinámico de Redis por entorno (`estoy-ok_dev-database-` vs `estoy-ok_production-database-`).
- **Archivo:** `backend/start.sh`
  - Supervisor del worker escuchando `default,dev_default`.

### B. Protección de Webhooks en Evolution API
- **Archivo:** `backend/app/Services/EvolutionApiService.php`
  - Dev no sobreescribe el webhook oficial en Evolution API. Producción mantiene el control y replica mensajes mediante Peer Mirroring.

### C. Auto-recuperación en caliente ante Error 1006 / Socket Cerrado
- **Archivo:** `backend/app/Services/EvolutionApiService.php`
  - Detección de caída de socket (`1006`, `close`, `is not open`, `Connection Closed`).
  - Polling activo de salud de conexión hasta 6 segundos tras invocar `reconnectInstance()`, garantizando estado `open` antes de enviar el reintento.

### D. Blindaje de Despacho en Planificador (`VerifyInactivity.php`)
- **Archivo:** `backend/app/Console/Commands/VerifyInactivity.php`
  - Bloque `try-catch (\Throwable $e)` alrededor de la creación y despacho de la alerta.
  - Si el encolado falla, se elimina la alerta huérfana y se libera el lock en caché para permitir que el planificador reintente automáticamente en el minuto siguiente.
  - Telemetría en caché: `last_verify_dispatch_attempt` y `last_verify_dispatch_error`.

### E. Scheduler Mutex Expiration
- **Archivo:** `backend/routes/console.php`
  - `Schedule::command('checkins:verify-inactivity')->everyMinute()->withoutOverlapping(10)` con expiración explícita de 10 minutos para evitar deadlocks de 24 horas.

---

## 4. Estado de los Tests Automatizados
- **Total tests:** `190 passed (640 assertions)`
- **Duración:** ~23 segundos
- **Comando:** `docker compose exec backend php artisan test`
- **Regresiones:** Cero regresiones detectadas.

---

## 5. Puntos de Partida para la Sesión de Mañana
1. **Verificación final con la app móvil instalada:**
   - Validar que al expirar el reloj en Dev, la notificación push llegue al teléfono de Tamara (`tamarakshop@gmail.com`) y que ambos mensajes de WhatsApp entren fluidamente.
2. **Respuesta "OK" en Dev:**
   - Responder "OK" desde el teléfono al mensaje recibido en Dev y constatar la renovación del reloj a 12 horas vía webhook mirroring.
3. **Limpieza de endpoints auxiliares de diagnóstico:**
   - Una vez concluida la validación con la app física, asegurar o retirar los endpoints auxiliares de `/maintenance/*`.
