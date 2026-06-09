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

### In Progress:
- [ ] **FASE 8: Gestión de Círculos y Seguridad Avanzada**
  - [ ] Interfaz de gestión de miembros del círculo.
  - [ ] Visualización de miembros en el mapa del Dashboard.
  - [ ] Alertas personalizadas por miembro.

### Next Steps:
- Implement Circles Management UI in Mobile/Web.
- Add "Circle Map" view to allow users to see their family members.
- Refine background location frequency vs battery consumption.
