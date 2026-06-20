# Project Status

## Current Phase: FASE 8: Gestión de Círculos y Seguridad Avanzada

### Completed Phases:
- [x] **FASE 1: Infraestructura de Contenedores**
  - [x] Docker Compose orchestration.
  - [x] Backend (PHP 8.4-FPM + Nginx).
  - [x] Database (PostgreSQL 16 + PostGIS).
  - [x] Frontend Web (Next.js 16).
  - [x] Cache & Queue (Redis).
  - [x] Worker & Scheduler dedicated services.
- [x] **FASE 2: Core de Supervivencia (MVP Pasivo)**
  - [x] Sanctum Authentication.
  - [x] Check-in API endpoints.
  - [x] Inactivity verification command.
  - [x] Inactivity alerts (Email/Push logic enqueued).
  - [x] Basic Filament Admin Panel (Users, Circles, Contacts).
  - [x] Swagger Documentation.
- [x] **FASE 3: Monetización y Pasarelas de Pago Multi-Región (Suscripción Premium)**
  - [x] Mercado Pago Integration (Argentina).
  - [x] Stripe Billing (International).
  - [x] PayPal Subscriptions (Alternative).
  - [x] Subscription models and `is_premium` logic synced.
- [x] **FASE 4: Canales Críticos de Comunicación (WhatsApp Automatizado)**
  - [x] Twilio SDK Integration.
  - [x] WhatsApp messaging Job for premium users.
  - [x] SMS fallback logic.
  - [x] Automated tests for alerts.
- [x] **FASE 5: Tracking Activo y Geofencing (Módulo Tipo Life360)**
  - [x] PostGIS endpoints for high-frequency coordinates.
  - [x] Geofencing logic (ST_DWithin) with anti-spam events.
  - [x] Automated Push Notifications for entry/exit.
  - [x] Verified with `GeofencingTest`.
- [x] **FASE 6: Visualización Web de Emergencia**
  - [x] Landing page and SEO metadata.
  - [x] Dynamic emergency routes (Next.js) with UUID.
  - [x] Map rendering with Leaflet for last known location.
  - [x] Crisis screen UI.
  - [x] Linked WhatsApp/Email alerts to public emergency view.
- [x] **FASE 7: Verificación de Email con OTP**
  - [x] Sistema de códigos de 6 dígitos (Backend).
  - [x] Endpoints de verificación y reenvío.
  - [x] Mailable con colas (Redis).
  - [x] Integración en App Mobile (Expo) y Web (Next.js).
  - [x] Swagger y Tests de integración.
- [x] **FASE Mobile: Estabilización y Expo Go**
  - [x] Migración a Expo SDK 54.
  - [x] Soporte para React 19 y React Native 0.81.
  - [x] Optimización de conectividad real-device (IP local).
- [x] **FASE 8: Gestión de Círculos y Seguridad Avanzada**
  - [x] Implementación completa de Alertas de Inactividad ("Estoy Ok") personalizables.
  - [x] Refactorización de UI (Web/Mobile) para separar Bienestar de Rastreo.
  - [x] Modo de pruebas rápidas (minutos) para desarrollo.
  - [x] Visualizador de contraseña (ojo) en login y registro (Web & Mobile).
  - [x] Reemplazo de alerts nativos por notificaciones Toast personalizadas en la Web.
  - [x] Banner de estado de bienestar persistente y cartel animado de éxito de check-in en la Web.
  - [x] Banner de estado de bienestar persistente ("Protegido y a Salvo", "Reporte Vencido", "Sin Reportes") en la App Mobile.
  - [x] Soporte para deslizar y actualizar (Pull-to-Refresh) en la pantalla de inicio Mobile.
  - [x] Resolución automática de alertas al hacer check-in, vista de tranquilidad y desactivación de mapa por privacidad (Web & Mobile).
  - [x] Recordatorios preventivos (`checkins:send-reminders`) vía Push y Email antes de expirar el umbral.
  - [x] Sanitización de teléfonos en backend (mutadores) y validación de prefijo `+` con texto de ayuda (Web & Mobile).
  - [x] Validación específica de formato de email y mensajes de error detallados (Web & Mobile).
  - [x] Endpoint GET /api/check-ins y registro histórico de reportes de bienestar en base de datos.
  - [x] Sección de "Historial de Reportes" en el Dashboard Web (Bienestar) y en la App Móvil (Bienestar).
  - [x] Modo Sueño (Horas Silenciosas) configurable con soporte para cruce de medianoche y pausa de avisos/recordatorios (Web, App & Backend).
  - [x] Webhook de Twilio para Check-in por SMS / WhatsApp y toggle de configuración en Web y Mobile.
  - [x] Alertas de inactividad escalonadas secuencialmente, reordenación de contactos y toggle de configuración en Web y Mobile.
  - [x] Interfaz de gestión de miembros del círculo.
  - [x] Visualización de miembros en el mapa del Dashboard.
  - [x] Alertas personalizadas por miembro.
  - [x] feat(crisis): Agregar feedback interactivo de contactos ("Voy en camino" / "Recibido") en mapa de emergencia.
    - [x] Backend: Creada tabla `emergency_responses`, endpoints de envío de feedback y de privacidad de respuestas de contacto, con tests de integración.
    - [x] Web: Entrada de nombre, botones de feedback ("Marcar como Leído", "Recibido / Enterado", "Voy en camino"), visualización condicional según la privacidad y polling de 10 segundos.
    - [x] Configuración (Web & Mobile): Toggle de privacidad "Compartir respuestas de apoyo" persistente.
  - [x] feat(wellbeing): Implementar auto-check-in pasivo basado en conexión a Wi-Fi segura y podómetro/sensores.
    - [x] Backend: Migraciones para configuración de automatización en `users` y `source` en `check_ins`, endpoints de API correspondientes, y tests.
    - [x] Web: Configuración de automatización en SecuritySettings y visualización de origen en historial.
    - [x] Mobile: Soporte de podómetro y detección de Wi-Fi, switches en ajustes, historial de origen y lógica de fondo en `locationTask.ts`.
  - [x] feat(branding): Rediseño de terminología para diferenciación del producto (Círculos a Núcleos, Geocercas/Perímetros a Zonas Seguras).
  - [x] feat(ux): Implementar FAQs interactivas en la landing page y tooltips de ayuda contextuales en el dashboard web.
  - [x] feat(tracking): Implementar alertas de batería baja para miembros del núcleo (Next.js, Expo, Laravel, y tests).
  - [x] feat(tracking): Mostrar el estado de sensores (GPS desactivado, Modo Avión, Rastreo Apagado).
    - [x] Backend: Migración base de datos con campos `is_tracking_active`, `gps_enabled`, `last_seen_at` y accessor `is_offline`, nuevos endpoints `/sensor-status` y tests.
    - [x] Web Dashboard: visualización de badges dinámicos ("Rastreo Apagado", "GPS Desactivado", "Sin Señal") en la lista de miembros y en los popups del mapa con menor opacidad en los marcadores inactivos.
    - [x] App Mobile: cola local de ubicaciones offline mediante AsyncStorage con sincronización/flush automático al recuperar la red, envío de cambios de estado a la API e indicadores visuales de sensores en el listado de miembros del núcleo.
    - [x] Landing Page: Promoción de la funcionalidad de monitoreo de sensores en la lista de características y en las respuestas de preguntas frecuentes (FAQs).
  - [x] feat(tracking): Corrección del centrado del mapa de historial, visualización de marcador de posición en la App Móvil, unificación de proveedor visual (Google Maps), cabeceras dinámicas ("Ubicación de..." vs "Ruta de...") y botón directo "Cómo llegar" para redirigir opcionalmente al GPS externo.

### In Progress:
- [ ] **Fase de Optimización de Batería y Ajustes de Ubicación (Fase 9)**
  - [ ] Refinar frecuencia de ubicación de fondo vs consumo de batería en la App Mobile.

### Next Steps:
- Refine background location frequency vs battery consumption in Mobile (Fase 9).
- Prepare staging and production deployment configurations.
- Implement advanced analytics/reports for premium users.

---

## 📘 Convenciones Adoptadas (UX y Nomenclatura)
Para futuras iteraciones de Estoy Ok, deben respetarse estrictamente los siguientes criterios de marca e interfaz de usuario:

1. **Nomenclatura (Naming):**
   * **Círculo / Círculos $\rightarrow$ Núcleo / Núcleos:** En el frontend (Web/Mobile), evitar la palabra "círculo" para no ser un clon de Life360.
   * **Geocercas / Perímetros $\rightarrow$ Zonas Seguras:** En la interfaz de cara al usuario final, evitar tecnicismos fríos. Utilizar "Zonas Seguras" para referirse a los radios configurados en el mapa.
   * *Nota técnica:* El backend mantiene el naming `circles` y `geofences` en base de datos y endpoints por razones de compatibilidad y simplicidad relacional.

2. **Ayuda contextual (UX):**
   * **Landing Page:** Secciones informativas generales con una sección final de FAQs estructurada en acordeones interactivos manejados con estado de React (`openFaqIndex`).
   * **Dashboard Web:** Uso de tooltips descriptivos flotantes (CSS puro con clases `group-hover` de Tailwind) al lado de opciones y configuraciones complejas para guiar al usuario sin sobrecargar de texto la interfaz.
   * **App Móvil:** Mantener la explicación del funcionamiento (microcopy) integrada de forma directa y sutil en texto pequeño debajo de cada toggle o switch de configuración, garantizando legibilidad y rapidez táctil.
