# Product Requirement Document (PRD)
## Proyecto: Plataforma de Seguridad Familiar Integral (Activa y Pasiva)

---

## 1. Visión General del Producto
El objetivo es desarrollar una plataforma de seguridad integral que combine dos conceptos de asistencia:
1. **Monitoreo de Bienestar Pasivo (Estilo Demumu/Sileme):** Un sistema de "Check-in" diario donde el usuario presiona un botón para confirmar que está bien. Si no lo hace en un período de tiempo configurado, el sistema dispara alertas automáticas a su grupo de emergencia.
2. **Monitoreo de Ubicación Activo (Estilo Life360):** Seguimiento en tiempo real sobre un mapa y alertas automáticas por ingreso/egreso de perímetros configurados (Geofencing).

### Stack Tecnológico Definido
*   **Backend:** Laravel (PHP 8.x) + Filament PHP (Panel de Administración).
*   **Base de Datos:** PostgreSQL + Extensión PostGIS (Para datos espaciales y geofencing).
*   **Frontend Web:** Next.js (Visualización de mapas de emergencia, landing page, SEO).
*   **Frontend Mobile:** Expo / React Native (Aplicación nativa para iOS y Android).
*   **Colas y Caché:** **Redis** (Crítico para notificaciones y encolado de pronósticos).
*   **Infraestructura:** Docker Compose (Local) / Debian o Ubuntu (Prod).

---

## 2. Arquitectura de Datos Base (PostgreSQL)
Para la correcta ejecución de las fases, el modelo de datos inicial debe contemplar:
*   `users`: Datos de perfil, `last_check_in_at` (timestamp indexado) y `expo_push_token`.
*   `circles` / `groups`: Grupos cerrados de seguimiento.
*   `circle_user`: Tabla pivote de pertenencia a grupos (roles: administrador, miembro).
*   `emergency_contacts`: Contactos vinculados a un usuario/círculo para recibir alertas externas.
*   `current_locations`: Guarda únicamente el último registro de posición global de cada usuario (Punto PostGIS).
*   `location_histories`: Historial de posiciones indexado por tiempo y particionado para usuarios Premium.

---

## 3. Plan de Desarrollo por Fases (Roadmap para Agente IA)

### FASE 1: Infraestructura de Contenedores (Docker Compose para Desarrollo)
*Objetivo: Orquestar todo el entorno local en contenedores independientes y estandarizados para garantizar la paridad de desarrollo entre el backend, el frontend web y la base de datos con soporte geoespacial.*

*   **Configuración del Entorno (Orquestación):**
    *   Diseñar un archivo `docker-compose.yml` en la raíz del proyecto estructurado en servicios aislados mediante una red interna común (`bridge`).
    *   Configurar variables de entorno optimizadas (`.env.example`) para mapear la comunicación fluida entre los contenedores sin hardcodear IPs locales.
*   **Servicio 1: Backend API & Backoffice (Laravel + Filament)**
    *   Utilizar una imagen oficial de **PHP 8.3/8.4-FPM** optimizada, instalando las extensiones necesarias para el proyecto (`pdo_pgsql`, `redis`, `gd`, `bcmath`, `zip`, `intl`).
    *   Configurar un contenedor secundario para **Nginx** que actúe como servidor web frente a Laravel, exponiendo el puerto `8000`.
    *   Configurar un contenedor dedicado para el **Laravel Queue Worker** que corra de forma persistente (`php artisan queue:work`) para procesar las colas asincrónicas de alertas de WhatsApp, emails y notificaciones.
    *   Configurar un contenedor para el **Laravel Scheduler** que simule el Cron Job del sistema corriendo cada 1 minuto (`php artisan schedule:work`) para testear localmente el disparador de inactividad de la Fase 1.
*   **Servicio 2: Base de Datos Geoespacial (PostgreSQL)**
    *   Utilizar la imagen oficial de **PostGIS** basada en la última versión estable de PostgreSQL (ej: `postgis/postgis:16-3.4` o superior).
    *   Configurar volúmenes persistentes locales para asegurar que los datos de prueba, usuarios y coordenadas no se pierdan al reiniciar los contenedores.
    *   Exponer el puerto nativo `5432` mapeado localmente para permitir auditorías externas de datos o conexiones directas desde herramientas de escritorio (DBeaver, TablePlus).
*   **Servicio 3: Frontend Web (Next.js)**
    *   Crear un contenedor basado en **Node.js 22-alpine** para levantar el entorno de desarrollo de Next.js.
    *   Configurar el montaje de volúmenes para habilitar el *Hot Reloading* de cambios en tiempo real en las vistas web de emergencia.
    *   Exponer el puerto `3000` hacia el host de desarrollo.
*   **Servicio 4: Cache y Mensajería Híbrida (Redis)**
    *   Levantar una instancia oficial de **Redis Alpine** ligera.
    *   Configurarlo en Laravel como el driver por defecto tanto para el manejo de las colas de trabajo (`QUEUE_CONNECTION=redis`) como para el almacenamiento en caché de sesiones o almacenamiento temporal de coordenadas de alta frecuencia.
*   **Directiva Especial para Mobile (Expo):**
    *   *Nota de arquitectura:* El entorno de desarrollo de Expo (React Native) se mantendrá corriendo **de forma nativa en la máquina host (fuera de Docker)**. Esto es estrictamente necesario para evitar conflictos de red complejos al exponer los puertos e interfaces inalámbricas requeridas por el código QR de Expo Go, las conexiones por *tunneling* (Ngrok/Expose) y el puente de depuración USB/Red con los dispositivos móviles físicos. La app de Expo consumirá la API de Laravel apuntando al contenedor de Nginx expuesto.









### FASE 2: Core de Supervivencia (MVP Pasivo)

*Objetivo: Implementar la lógica del botón diario "Estoy OK" con alertas básicas por inactividad.*

*   **Backend (Laravel + Filament):**
    *   Configurar autenticación API con Laravel Sanctum para la app mobile.
    *   Crear endpoints para `POST /api/check-in` que actualicen el timestamp `last_check_in_at`.
    *   Crear un Comando de Consola de Laravel (`checkins:verify-inactivity`) configurado en el Scheduler para ejecutarse cada 30 minutos. Este comando identificará usuarios inactivos que hayan superado el umbral (24 horas por defecto).
    *   Implementar cola de trabajo (`Queue`) para procesar el envío de alertas de inactividad de forma asrónica mediante Email (Resend/SendGrid) y Notificaciones Push a través de la API de Expo (`https://exp.host/--/api/v2/push/send`).
    *   Panel básico en Filament para visualizar usuarios y estados de check-in.
*   **Mobile (Expo):**
    *   Maquetado de pantalla principal minimalista con un botón central de gran tamaño.
    *   Consumo de la API de check-in mediante fetch/axios apuntando al backend local.
    *   Configuración básica de `expo-notifications` para recibir recordatorios de cortesía previos al vencimiento del plazo.
*   **Pruebas de Desarrollo:** Testeo mediante **Expo Go** en red Wi-Fi local o utilizando URLs públicas temporales expuestas con Expose/Ngrok.

### FASE 3: Monetización y Pasarelas de Pago Multi-Región (Suscripción Premium)
*Objetivo: Habilitar el modelo Freemium integrando pasarelas de pago adaptadas tanto al mercado local (Argentina) como internacional.*

*   **Estrategia de Suscripción:**
    *   **Plan Gratis:** Check-in fijo cada 24 horas, alertas de inactividad solo por Email/Push, mapa en tiempo real con historial limitado a 48 horas y máximo 2 perímetros (geofences).
    *   **Plan Premium:** Ventanas de tiempo de check-in editables, alertas críticas por canales prioritarios, historial de mapas extendido a 30 días, geofences ilimitados.
*   **Integración de Pasarelas de Pago en Laravel:**
    *   **Mercado Pago (Suscripciones / Webhooks):** Integración prioritaria para el mercado argentino mediante la SDK oficial. Manejo de cobros recurrentes en pesos y procesamiento de Webhooks para activar/suspender el flag `is_premium` en el usuario.
    *   **Stripe Billing:** Integración mediante Laravel Cashier para cobros internacionales recurrentes con tarjeta de crédito. Manejo del portal de facturación de Stripe y eventos de webhook (`customer.subscription.deleted`, `invoice.payment_succeeded`).
    *   **PayPal Subscriptions:** Como alternativa internacional para usuarios que prefieran no ingresar tarjetas directamente.
*   **Sincronización:** Reflejar de inmediato el estado de suscripción del usuario en los frontends (Next.js y Expo) restringiendo o liberando componentes de la UI según su nivel de acceso.

### FASE 4: Canales Críticos de Comunicación (WhatsApp Automatizado)
*Objetivo: Asegurar que las alertas por inactividad lleguen a los familiares a través del canal de mayor lectura en Latinoamérica.*

*   **Backend (Laravel):**
    *   Integración con la API oficial de WhatsApp (a través de **Twilio** o **Meta Cloud API** directamente) exclusiva para cuentas con suscripción Premium activa.
    *   Configuración y validación de plantillas de mensaje (`Templates`) homologadas ante Meta para evitar baneos por spam. Ejemplo de estructura: *"Aviso de seguridad: {{1}} no ha reportado actividad en la aplicación durante las últimas {{2}} horas. Por favor, intente contactarlo."*
    *   Desarrollo de lógica en el script de inactividad: Si el usuario es Premium, se despacha el Job de WhatsApp prioritario.
*   **Estrategia de Contingencia:** Lógica de reintentos automatizada en la cola de Laravel. Si la API de WhatsApp falla o devuelve error, el sistema degrada la alerta hacia un SMS de respaldo.

### FASE 5: Tracking Activo y Geofencing (Módulo Tipo Life360)
*Objetivo: Implementar el mapa en tiempo real y la recolección inteligente de coordenadas.*

*   **Mobile (Expo Development Builds):**
    *   Migrar el entorno de desarrollo de Expo Go hacia un **Development Build** (`expo-dev-client`) para compilar binarios de desarrollo propios. Esto es estrictamente necesario para incorporar permisos avanzados de sistema de fondo.
    *   Implementar `expo-location` y `expo-task-manager` para rastrear la ubicación GPS del dispositivo de manera persistente en segundo plano (incluso con la app cerrada o el teléfono bloqueado).
    *   Optimizar la recolección de coordenadas: Enviar actualizaciones a la API de Laravel basándose en umbrales de movimiento (ej. cada 100 metros) o intervalos de tiempo adaptativos para resguardar la batería.
*   **Backend (Laravel + PostGIS):**
    *   Activar la extensión espacial PostGIS mediante migraciones de base de datos.
    *   Crear endpoints de alta frecuencia para recibir las coordenadas de los dispositivos y actualizar la tabla `current_locations` utilizando tipos geométricos nativos.
    *   Desarrollar la lógica de Geofencing en PostgreSQL para detectar si la posición reportada interseca con los perímetros de alertas creados por los círculos de usuarios (mediante funciones como `ST_Distance` o `ST_Contains`). Disparar alertas push automáticas de ingreso/salida.

### FASE 6: Visualización Web de Emergencia
*Objetivo: Proveer un canal web inmediato de acceso rápido para familiares en situaciones de alerta.*

*   **Frontend Web (Next.js):**
    *   Crear un sitio web optimizado para carga rápida y SEO.
    *   Desarrollar una ruta dinámica segura (ej: `/emergencia/[id-alerta-encriptada]`). Cuando se dispare una alerta de inactividad, el mensaje de WhatsApp automático incluirá este link.
    *   Al abrir el link desde cualquier navegador móvil (sin requerir que el familiar tenga la app instalada), Next.js procesará mediante Server-Side Rendering (SSR) una pantalla de crisis de carga ultra rápida que renderizará un mapa (usando Leaflet o Mapbox) reflejando la última ubicación registrada del usuario inactivo.

---

## 4. Requerimientos No Funcionales (Technical Constraints)

### 4.1. Rendimiento y Escalabilidad

* **Cola de Escritura:** Implementación obligatoria de **Redis Queue** para procesar tareas criticas.
* **Latencia:** Acciones críticas < 2 segundos (percibido por usuario gracias a Optimistic UI).
* **Caché:** Uso agresivo de CDN para assets estáticos.

### 4.2. Disponibilidad e Integridad

* **SLA:** 99.9% durante momentos de alto transito en el sitio.

### 4.3. Usabilidad (UX)

* **Mobile First:** Diseño responsivo.
* **PWA:** Soporte de `manifest.json`.
* **Feedback de Estado:** Indicadores claros de "Guardando...", "Guardado" y "Cerrado" cuando corresponda



### 4.4. Metodología "Agent-First" (Mandatoria)

Orden estricto de desarrollo para evitar alucinaciones:

1. **API Contract:** Definir Modelos y Endpoints.
2. **Documentación:** Generar **OpenAPI (Swagger)** automáticamente.
3. **Generación de Cliente:** Entregar el Swagger al Agente para generar servicios/hooks.
4. **UI:** Construir interfaz sobre los servicios.

### 4.5. Estructura del Repositorio (Monorepo GitHub)

- `/backend`: API en Laravel, migraciones, workers y configuración de Redis/Postgres.
- `/frontend-web`: Proyecto Next.js (PWA soportada con `manifest.json`).
- `/frontend-mobile`: Proyecto Expo React Native.
- `/docs`: Especificaciones, Swagger, assets y este PRD.





## 5. Instrucciones para el Agente (Gemini CLI / Ralph)

Cuando ejecutes las tareas asociadas a este PRD, tené en cuenta las siguientes directivas de desarrollo:
1.  **Priorizá la modularidad:** Empezá estrictamente resolviendo la API de Laravel de la **Fase 1** (migraciones básicas de usuarios, checks e inactividad) antes de avanzar al código de Expo.
2.  **Manejo de coordenadas:** Utilizá funciones crudas de base de datos o paquetes compatibles con PostGIS en Laravel para la manipulación de puntos geográficos. No proceses cálculos de distancias complejas directamente dentro de colecciones de PHP.
3.  **Resiliencia en segundo plano:** Todo el software desarrollado para la tarea programada (`Cron Job`) y el consumo de APIs externas de pasarelas de pago y mensajería debe estar encapsulado en bloques `try/catch` robustos con logging detallado y despacho automático hacia colas de reintento (`Queues`).