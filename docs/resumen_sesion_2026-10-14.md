# Resumen de Sesión - 14 de Octubre de 2026 (Mañana)
**Objetivo principal:** Diagnóstico de raíz, blindaje e implementación de la entrega de alertas de inactividad de bienestar (reloj vencido) con espaciado técnico para mismo número y verificación en hardware real tanto en **Producción** (`api.estoyok24.com`) como en **Desarrollo** (`backend-api-dev-2a56.up.railway.app`).

---

## 1. Diagnóstico del Problema y Hallazgos Clave

### A. Contexto Inicial y Síntoma
1. **Entorno de Producción (`nicolas@gmail.com`):**
   * Las alertas SOS manuales disparadas desde el botón de la app llegaban de inmediato (~2 segundos) a los contactos de emergencia gracias al uso de `SendSosAlertJob::dispatchSync`.
   * Sin embargo, al expirar el reloj de bienestar de forma natural a las 05:40:00 (08:40:00 UTC), se persistió la alerta `EmergencyAlert` en PostgreSQL (`01a13991-8571-70b9-bc2b-4a7724d818ca`), pero **ningún mensaje de WhatsApp fue despachado**.
   * Al forzar el envío sincrónico directo (05:23 y 05:45), ambos mensajes salieron en ~6 a 9 segundos con código `HTTP 201` en Evolution API v2.3.7 y fueron recibidos inmediatamente en el celular del usuario (`+54 9 11 4979-0220`).

### B. Análisis Cronológico y Descarte de Errores Posteriores
Se evaluó deductivamente la hipótesis de si una falla en el canal de correos electrónicos (SMTP / Mailgun) bloqueaba el flujo:
* La secuencia cronológica interna de `SendInactivityAlerts::handle` es:
  1. *Push Notification*
  2. *WhatsApp propio* (si `notify_self_whatsapp_on_inactivity` está activo)
  3. *Pausa técnica*
  4. *Bucle de familiares en el núcleo*
  5. *WhatsApp a los contactos de emergencia*
  6. *Envío de correos electrónicos por SMTP*
* **Deducción lógica irrefutable:** Si el paso 6 (Email) fuera el causante de una falla, los pasos previos 2 y 5 (ambos mensajes de WhatsApp) ya habrían sido enviados y recibidos en el dispositivo antes de intentar el correo.
* La ausencia total de WhatsApps a las 05:40:00 demostró de forma contundente que la ejecución **jamás llegó a entrar al método `handle()`** de `SendInactivityAlerts`.

### C. Causa Raíz
1. **Worker de Colas de Redis Congelado en Railway Producción:**
   * En el contenedor de Producción de Railway, el proceso demonio en segundo plano `queue:work` ejecutado por `start.sh` se encontraba en estado dormido / bloqueado.
   * Con `/api/maintenance/test-job-dispatch?email=nicolas@gmail.com`, se comprobó que los trabajos encolados asíncronamente en `queues:default` permanecían atascados (`default_len: 1`) sin ser consumidos.
2. **Dependencia Crítica del Proceso Scheduler en Memoria:**
   * Al depender de workers asíncronos en colas de Redis, cualquier fallo o suspensión del worker paralizaba las alertas de emergencia.
   * En cambio, el despacho síncrono directo (`dispatchSync`) en el mismo hilo de ejecución elimina el intermediario de Redis y garantiza la entrega inmediata e incondicional hacia Evolution API.

---

## 2. Implementación de la Solución (Plan Aprobado)

### A. Desacoplamiento Total de Redis en Alertas Críticas
* **Archivo:** [`backend/app/Console/Commands/VerifyInactivity.php`](file:///home/usuario/aplicaciones/estoyok/backend/app/Console/Commands/VerifyInactivity.php)
* Se adoptó la misma filosofía infalible de SOS y choques (`dispatchSync`): `checkins:verify-inactivity` ejecuta `SendInactivityAlerts::dispatchSync` de forma incondicional en entornos reales (manteniendo `dispatch` en unit tests con `Queue::fake()`).

### B. Blindaje de Mismo Número y Espaciado Técnico (5 Segundos)
* **Archivo:** [`backend/app/Jobs/SendInactivityAlerts.php`](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/SendInactivityAlerts.php)
* **Problema atendido:** Cuando el titular y el contacto de emergencia comparten el mismo número físico (como en las pruebas o auto-monitoreo con `+54 9 11 4979-0220`), el protocolo de cifrado de WhatsApp (Signal / Double Ratchet en Baileys) colisiona si dos mensajes salen con menos de 4 o 5 segundos de diferencia, generando bloqueos de sesión o el cartel *"Esperando el mensaje"*.
* **Solución implementada:**
  1. Creada la función `normalizePhone()` que estandariza los teléfonos al formato canónico internacional (ej: `5491149790220`).
  2. Implementada la detección de igualdad entre el último número despachado y el siguiente destinatario.
  3. **Pausa obligatoria de 5 segundos completos (`sleep(5)`)** si el destinatario es el mismo número, permitiendo que WhatsApp ratchee la clave criptográfica.
  4. Espaciado ágil de **1.5 segundos (`usleep(1500000)`)** entre números distintos para máxima velocidad.

### C. Aislamiento por Bloques Try-Catch Independientes por Canal
* En `SendInactivityAlerts.php`, cada canal (Push, WhatsApp propio, WhatsApp contactos y Email) fue encapsulado en su propio bloque `try-catch (\Throwable $e)` con logging dedicado.
* Si un canal secundario (como SMTP o un token Push vencido) falla, **jamás aborta ni interrumpe la entrega de los mensajes de WhatsApp**.

### D. Resiliencia de Contenedores y Auto-Refresco de Configuración
* **Archivos:** [`backend/start.sh`](file:///home/usuario/aplicaciones/estoyok/backend/start.sh) y [`backend/app/Services/EvolutionApiService.php`](file:///home/usuario/aplicaciones/estoyok/backend/app/Services/EvolutionApiService.php)
* Agregado `php artisan config:clear` en el script de arranque para eliminar cachés estáticas generadas durante el build de Nixpacks en Railway.
* Agregadas banderas `--max-time=3600 --memory=128` a `queue:work` para reciclar limpiamente los workers cada hora.
* Incorporados fallbacks dinámicos en tiempo de ejecución en `EvolutionApiService` para resolver `EVOLUTION_API_KEY` desde el entorno aunque el config estático no esté disponible.

---

## 3. Protocolo de Validación en Vivo (Hardware Real)

### A. Pruebas en Producción (`api.estoyok24.com` - `nicolas@gmail.com`)
1. **Prueba 1 (06:27):**
   * Reloj configurado a 1 minuto. Vencimiento natural a las 06:26:12.
   * Disparo automático del scheduler a las **06:27:00**.
   * Mensaje 1 (auto-aviso) enviado a las **06:27:01**.
   * Pausa técnica de 5 segundos ejecutada.
   * Mensaje 2 (aviso con link de rescate) enviado a las **06:27:07** (`HTTP 201`).
   * **Respuesta "OK":** El usuario respondió "OK" desde WhatsApp, el reloj se restableció a 12 horas y llegó de inmediato la confirmación (*«✅ Bienestar verificado con éxito en Estoy Ok. ¡Gracias!»*).
2. **Prueba 2 (06:57):**
   * Segunda corrida consecutiva en Producción para verificar consistencia.
   * Disparo a las **06:57:00** $\rightarrow$ Mensaje 1 a las **06:57:01** $\rightarrow$ Pausa de 5s $\rightarrow$ Mensaje 2 a las **06:57:07** (`HTTP 201`).
   * Ambos mensajes recibidos de inmediato en el celular sin demoras.

### B. Pruebas en Desarrollo (`backend-api-dev-2a56.up.railway.app` - `tamarakshop@gmail.com`)
1. **Prueba Final (07:07):**
   * Tras desplegar los fallbacks de configuración y el nuevo contenedor en Dev (`hostname: 8a281bfc787f`).
   * Reloj configurado a 1 minuto. Vencimiento natural a las 07:06:05.
   * Disparo automático del scheduler a las **07:07:00**.
   * Mensaje 1 enviado a las **07:07:01**.
   * Pausa técnica de 5 segundos ejecutada.
   * Mensaje 2 enviado a las **07:07:06** (`HTTP 201`).
   * **Tiempo total de ejecución:** 6 segundos.
   * Ambos mensajes recibidos con éxito en el WhatsApp del usuario.

---

## 4. Estado de la Suite de Tests Automatizados
* **Tests:** `190 passed (640 assertions)`
* **Duración:** ~17.74 segundos
* **Regresiones:** Cero regresiones detectadas (`docker compose exec backend php artisan test`).

---

## 5. Puntos de Partida para la Sesión de la Tarde
1. **Estado del Sistema:**
   * Tanto **Producción** como **Desarrollo** quedaron 100% operativos, alineados y verificados en hardware real de punta a punta.
   * El ciclo completo de reporte de bienestar vencido, doble mensaje de WhatsApp con espaciado de 5s, respuesta con "OK" y reseteo del reloj a 12h con confirmación funciona de forma completamente autónoma.
2. **Siguientes tareas planificadas:**
   * Limpieza de endpoints auxiliares de diagnóstico `/api/maintenance/*` creados para la telemetría de pruebas.
   * Continuar con la **Fase 14 (Depreciación de Web Funcional y Enfoque Móvil Exclusivo)** según [`docs/plan_depreciacion_web.md`](file:///home/usuario/aplicaciones/estoyok/docs/plan_depreciacion_web.md).
