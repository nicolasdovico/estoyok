# Project Status

## Current Phase: FASE 5: Tracking Activo y Geofencing (Módulo Tipo Life360)

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

### In Progress:
- [ ] **FASE 5: Tracking Activo y Geofencing (Módulo Tipo Life360)**
  - [ ] Development build for Expo (Background tasks).
  - [ ] `expo-location` and `expo-task-manager` implementation.
  - [ ] PostGIS endpoints for high-frequency coordinates.
  - [ ] Geofencing logic (ST_Distance/ST_Contains).

### Next Steps:
- Configure PostGIS extension in migrations if not already done.
- Create endpoints for location tracking.
- Implement background location task in Expo (Mobile).
