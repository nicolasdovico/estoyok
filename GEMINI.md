# Estoy Ok 

## Project Overview
Estoy Ok is a comprehensive family safety platform (active and passive) that combines two types of assistance. The first, Active, includes a real-time tracking system on a map and automatic alerts when entering or exiting configured perimeters. The second, Passive, features a daily “check-in” system where the user presses a button to confirm they are okay. If they do not do so within a configured time period, the system triggers automatic alerts to their emergency contact . The project is designed for social virality and simplicity, following an **Agent-First** development methodology to leverage AI assistance efficiently.

### Core Architecture
- **API-First & Decoupled:** Strict separation between the Backend (Business Logic) and Frontend (UI).
- **Agent-First:** Development follows a strict order (API Contract -> Swagger -> Frontend) to provide AI agents with precise context and minimize hallucinations.
- **Monorepo Structure (Planned):**
  - `/backend`: API en Laravel, migraciones, workers y configuración de Redis/Postgres.
  - `/frontend-web`: Proyecto Next.js (PWA soportada con `manifest.json`).
  - `/frontend-mobile`: Proyecto Expo React Native.
  - `/docs`: Especificaciones, Swagger, assets y este PRD.Tech Stack

- **Backend:** Laravel 12 (PHP 8.4), PostgreSQL 16, Redis (for queuing and cache).
- **Base de Datos:** PostgreSQL + Extensión PostGIS (Para datos espaciales y geofencing).
- **Admin Panel:** Laravel Filament.
- **Frontend Web:** Next.js (Visualización de mapas de emergencia, landing page, SEO).
- **Frontend Mobile:** Expo / React Native (Aplicación nativa para iOS y Android).
- **Documentation:** OpenAPI 3.0 (Swagger) via L5-Swagger.
- **Authentication:** Google OAuth 2.0 & Email/Password (Sanctum).
- **Infrastructure:** Custom Docker Compose (No Sail).

## Building and Running
Las instrucciones a continuación son válidas para el entorno de desarrollo y producción actual.

### Backend
- **Setup:** `docker compose up -d`
- **Migrations:** `docker compose exec backend php artisan migrate --seed`
- **Swagger:** `docker compose exec backend php artisan l5-swagger:generate`
- **Tests:** `./test.sh` (corre todos los tests) o `docker compose exec backend php artisan test`

### Frontend
- **Setup:** `npm install`
- **Start:** `npx expo start` (Local) o `docker compose restart frontend` (Vía Docker)
- **Build Web:** `npx expo export --platform web` (Genera carpeta `dist/`)
- **Tests:** `docker compose exec frontend npm test`

## Development Conventions
1. **API-First Workflow:**
   - Siempre definir/actualizar el contrato API en el backend antes de implementar features en el front.
   - Entregar el `swagger.json` al agente para generar servicios y hooks.
   
   
   
3. **SEO y Metadatos (Web):**
   - **Importación:** Importar el componente `Head` siempre desde `expo-router/head`.
   - **Rutas Públicas:** Cada pantalla pública debe definir su `<Head>` con título, descripción y etiquetas OpenGraph.
   - **Datos Estructurados:** Implementar JSON-LD (Schema.org) en pantallas que listen eventos o torneos.
   
4. **Accesibilidad y Semántica Web:**
   - Utilizar `accessibilityRole` (ej: 'header', 'main', 'footer', 'heading') en los componentes `View` y `Text` para asegurar que los crawlers y lectores de pantalla entiendan la jerarquía del sitio.
   
5. **Assets Estáticos:**
   - Los archivos estáticos para la web (robots.txt, favicon, etc.) viven en `frontend/public/`.
   
6. **Concurrency & Performance:**
   - Implementar **Optimistic UI** en el frontend.
   
8. **Mantenimiento de Estado:**
   - Es OBLIGATORIO actualizar `docs/status.md` y `docs/progress.txt` al completar tareas o cambiar de fase.
   
9. **Garantía de No Regresión (CRITICAL):**
   - Ejecutar `./test.sh` antes de finalizar cualquier tarea. Prioridad absoluta a corregir regresiones.

## Key Documentation
- `docs/prd.md`: Product Requirements Document (MVP).
- `docs/metodologia.md`: "Agent-First" development workflow.
- `docs/old/`: Legacy documentation and draft notes.

## Aditional Notes

- If you use docker compose, always run `docker compose` instead of `docker-compose` to avoid version conflicts.
- If you need to run a command inside a container, use `docker compose exec <service> <command>` instead of `docker exec` to ensure it works with the current Docker Compose version.
- if you must be a commit, always be a conventional commit. Additionally, ensure that each commit grouped logical changes, performing multiple commits if was necessary.
- Every time you change code, check for potential permission issues outside the container.