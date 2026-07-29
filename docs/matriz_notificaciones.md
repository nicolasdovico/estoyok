# Matriz y Catálogo de Notificaciones Programadas - Estoy Ok

Este documento especifica la arquitectura, eventos, destinatarios, canales de envío y requisitos de plan de todas las notificaciones automáticas y programadas por código en la plataforma **Estoy Ok**.

---

## 📋 Matriz Comparativa de Notificaciones

| # | Nombre del Evento (Disparador) | A quién le llega (Receptor) | Medio por el que llega (Canal) | Tipo de Usuario Requerido |
| :-: | :--- | :--- | :--- | :--- |
| **1** | **Verificación de Email (OTP)** - *(Registro inicial o reenvío de código)* | Propio usuario recién registrado | Email (`OtpVerificationMail`) | **Todos** (Gratis y Premium) |
| **2** | **Recordatorio Preventivo de Check-in** - *(Aviso previo a vencer reporte 'Estoy OK')* | Propio usuario | Push Notification y Email (`CheckInReminderMail`) | **Todos** (Gratis y Premium) |
| **3** | **Alerta de Inactividad ("Estoy OK")** - *(Vencimiento del reporte pasivo)* | Propio usuario, Miembros del Núcleo y Contactos SOS | Push (al usuario y Núcleo), Email y WhatsApp / SMS (a contactos SOS) | Push y Email: **Todos** / WhatsApp y SMS: **Solo Premium** |
| **4** | **S.O.S. Silencioso de Emergencia** - *(Pulsación del botón SOS en App Móvil)* | Miembros del Núcleo y Contactos SOS | Push Alta Prioridad (al Núcleo), SMS Urgente y WhatsApp (a contactos SOS) | Push y SMS: **Todos** / WhatsApp: **Solo Premium** |
| **5** | **Detección de Accidente Vehicular (Crash)** - *(Impacto fuerte de 4.5G o más)* | Miembros del Núcleo y Contactos SOS | Push Alta Prioridad (al Núcleo), SMS Urgente y WhatsApp (a contactos SOS) | Push y SMS: **Todos** / WhatsApp: **Solo Premium** |
| **6** | **Entrada / Salida de Zona Segura** - *(Cruce de perímetro de geocerca fija)* | Miembros del Núcleo | Push Notification | **Todos** (Gratis y Premium) |
| **7** | **Alerta de Radar Móvil / Proximidad** - *(Cruce de radio dinámico entre familiares)* | Usuario que activó el radar | Push Notification | **Todos** (Gratis y Premium) |
| **8** | **Alerta de Batería Baja (15% o menos)** - *(Batería crítica de un familiar)* | Miembros del Núcleo | Push Notification | **Todos** (Gratis y Premium) |
| **9** | **Exceso de Velocidad Vehicular** - *(Superación del límite fijado para el Núcleo)* | Miembros del Núcleo | Push Notification | **Todos** (Gratis y Premium) |
| **10**| **Recordatorio Fin de Prueba Gratis** - *(Aviso en el Día 5 de la prueba)* | Propio usuario | Push Notification y Email (`TrialExpiringSoonMail`) | Usuarios en **Prueba Gratis (Trial)** |
| **11**| **Suscripción Suspendida** - *(Fin del período de gracia tras pago fallido)* | Propio usuario | Push Notification y Email (`SubscriptionSuspendedMail`) | Usuarios con suscripción fallida |

---

## 📌 Detalle de Reglas y Arquitectura de Notificaciones

### 1. Diferencia entre Destinatarios: Núcleos vs Contactos SOS
* **Miembros del Núcleo (`circles`):** Comparten el mapa en tiempo real, geocercas, velocidad y batería. Reciben **Push Notifications** instantáneas en la App Móvil para estar al tanto del estado cotidiano del grupo y responder ante alertas de crisis.
* **Contactos SOS / Emergencia (`emergency_contacts`):** Son las personas designadas explícitamente por el usuario para ser notificadas en caso de inactividad o emergencias críticas. Se les notifica por **Email, WhatsApp y SMS** con el enlace dinámico a la pantalla pública de emergencia (`/emergencia/{id}`).

### 2. Respaldo y Fallback de Canales
* En eventos de crisis (SOS, Accidentes e Inactividad Premium), la plataforma intenta enviar **WhatsApp** vía Twilio SDK. Si la entrega a WhatsApp falla o la red no responde, el sistema ejecuta de forma transparente un **fallback automático a SMS** nativo.

### 3. Escalado Secuencial de Contactos
* Cuando la función de *Escalado de Inactividad* está activa, en lugar de notificar a todos los contactos de emergencia simultáneamente, se notifica al contacto de prioridad 1 y se programa el envío al contacto de prioridad 2 tras N minutos (`escalation_interval_minutes`). El ciclo se cancela automáticamente si el usuario realiza su check-in.

---

## 💻 Ubicación de las Clases y Jobs en el Backend (Laravel)

* **Inactividad ("Estoy OK"):** `app/Console/Commands/VerifyInactivity.php` & `app/Jobs/SendInactivityAlerts.php`
* **Recordatorios Preventivos:** `app/Console/Commands/SendCheckInReminders.php`
* **SOS Silencioso:** `app/Http/Controllers/EmergencyAlertController.php` (`POST /api/emergency-alerts/sos`)
* **Detección de Crash:** `app/Jobs/SendCrashAlertJob.php` (`POST /api/emergency-alerts/crash`)
* **Geocercas y Zonas Seguras:** `app/Jobs/ProcessGeofencing.php` y `app/Jobs/ProcessDynamicGeofencing.php`
* **Alertas de Batería Baja:** `app/Jobs/SendBatteryAlertJob.php`
* **Exceso de Velocidad:** `app/Jobs/SendSpeedingAlertJob.php`
* **Suscripciones y Ciclo de Vida:** `app/Console/Commands/SendTrialReminders.php` y `app/Console/Commands/CheckExpiredGracePeriods.php`
* **Servicio de Push Híbrido (Expo / FCM):** `app/Services/PushNotificationService.php`
