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
  - [x] feat(tracking): Corrección del centrado del mapa de historial, visualización de marcador de posición en la App Móvil, unificación de proveedor visual (Google Maps), cabeceras dinámicas ("Ubicación de..." vs "Ruta de..."), botón directo "Cómo llegar" para redirigir opcionalmente al GPS externo, y promoción/corrección de la funcionalidad de historial en la Landing Page comercial.
  - [x] feat(crisis): SOS silencioso con envío de ubicación en alta frecuencia y streaming de audio.
    - [x] Backend: Migración para añadir `audio_path` a la base de datos, nuevos endpoints `POST /api/emergency-alerts/sos` y `POST /api/emergency-alerts/{id}/audio` con almacenamiento en disco, generación de OpenAPI/Swagger y suite completa de tests de integración.
    - [x] Web: Cabecera parpadeante roja de alerta de SOS activo en la pantalla de crisis, renderizado de reproductor de audio ambiental para la grabación de 15s, marcas visuales parpadeantes en miembros y polling de ubicación acelerado a 5s.
    - [x] Mobile: Botón destacado de SOS silencioso de emergencia, grabación de fondo de 15s de audio con `expo-av`, cambio dinámico a tracking de ubicación de alta frecuencia (5s) y canal de respaldo mediante envío de SMS local.
    - [x] Mobile: Corregida la rotura de estilos del listado de miembros al activar/desactivar S.O.S., reemplazando el componente View del badge por un Text inline, ocultando acciones redundantes para el propio usuario y sincronizando el estado `isSosActive` inmediatamente al activar.
    - [x] Web Landing Page: Promoción y explicación del S.O.S. Silencioso (agregado como tercer pilar de seguridad de crisis activa en la grilla de características, en los beneficios del plan PRO Premium y en las FAQs interactivas).
  - [x] feat(drive): Detección automática de trayectos vehiculares y exceso de velocidad (Next.js, Expo, Laravel y tests).
    - [x] Backend: Migraciones para la tabla `drive_events`, columna `speed_limit` en `circles` y `speed` / `is_driving` en `current_locations` y `location_histories`.
    - [x] Backend API: Conversión de velocidad m/s a km/h, inicio/cierre automático de trayectos de conducción, y notificaciones Push (Expo) con rate-limiting mediante caché.
    - [x] Backend Console: Comando `drive:cleanup-active` programado en el Scheduler para cerrar trayectos huérfanos sin actualizar hace más de 30 minutos.
    - [x] Mobile: Captura de velocidad en `locationTask.ts`, histéresis mediante AsyncStorage (1 min inicio, 2 min tolerancia para detención) y envío al backend.
    - [x] Mobile UI & Settings: Badge inline con coche y velocidad en la lista de miembros de la home, y configuración para ajustar el límite de velocidad por núcleo en ajustes.
    - [x] Mobile Multiplatform: Corregido error de importación de `react-native-maps` en la compilación web de Metro extrayendo el mapa de historial a un componente con extensiones específicas de plataforma (`HistoryMap.tsx` y `HistoryMap.web.tsx`).
    - [x] Web: Marcador interactivo de coche (`🚗`) animado en `CircleMap.tsx` con colores condicionales según el límite excedido, badges inline de velocidad en el sidebar, formulario de ajuste para administradores, y promoción con características y FAQs en la Landing Page comercial (`page.tsx`).
  - [x] feat(drive): Detección automática de accidentes vehiculares mediante acelerómetro (Next.js, Expo, Laravel y tests).
  - [x] feat(geofencing): Implementar geocercas móviles y alertas de proximidad relativas (Next.js, Expo, Laravel y tests).
    - [x] Backend: Creadas tablas, modelo, job de cálculo de distancia geodésica PostGIS y endpoints de geocercas móviles con suite de tests.
    - [x] Mobile: Alternancia automática de frecuencia de GPS a alta fidelidad (5s), toggle de privacidad en ajustes y modal/vibración de alerta de proximidad.
    - [x] Web: Renderizado de círculo punteado, línea de trayectoria y tooltip dinámico en Leaflet.
  - [x] feat(ux): Rediseño y redistribución del layout móvil en 4 pestañas (Panel, Mapa, Núcleo, Ajustes) utilizando navegación basada en estados para preservar el rastreo de fondo.
  - [x] **Estabilización de Túnel ngrok y Depuración de Errores de Red:**
    - [x] Mobile & Docs: Creada guía de configuración para utilizar un túnel ngrok personalizado y con dominio fijo, resolviendo la desconexión y límites de la cuenta compartida de Expo.
    - [x] Mobile: Añadida validación de token de sesión activa (`auth_token`) en la tarea de segundo plano (`locationTask.ts`) para evitar peticiones no autorizadas y spamear errores 401 en la consola.
    - [x] Mobile: Modificada la severidad de logs de errores de red a normales (`console.log`) en la sincronización offline y auto-checkins para prevenir pantallas rojas intrusivas en el celular al perder señal temporalmente.
    - [x] Railway/Backend: Corregida la variable de entorno `FRONTEND_URL` para que apunte al dominio real del frontend en producción, solucionando el error 404 en el enlace de localización del mensaje SOS por WhatsApp.
    - [x] Mobile: Corregido crash `TypeError: activePoint.accuracy.toFixed is not a function` en [history.tsx](file:///home/usuario/aplicaciones/estoyok/mobile/src/app/history.tsx) al castear y formatear de manera segura la precisión (`accuracy`) a tipo numérico.
    - [x] Web & Mobile: Agregado el indicador visual de "Visto por última vez" en la lista lateral de miembros (Web en [Dashboard.tsx](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/components/Dashboard.tsx#L1488-L1496)) y en el listado principal de familiares (App en [index.tsx](file:///home/usuario/aplicaciones/estoyok/mobile/src/app/index.tsx#L1186-L1196)) para conocer la hora exacta del último reporte de ubicación recibido sin necesidad de hacer clic en el marcador del mapa.
    - [x] Web: Corregido error de compilación TypeScript en la Web al agregar `recorded_at` a la interfaz `current_location` en [Dashboard.tsx](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/components/Dashboard.tsx) y [CircleMap.tsx](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/components/CircleMap.tsx).
- [x] **FASE 10: Cobros y Membresías Premium**
  - [x] Issue 1: Diseñar la pantalla de selección de planes (Gratis vs Premium PRO) y el selector de pasarela de pago (Stripe, Mercado Pago, PayPal) en Next.js con redirección asíncrona a checkouts.
  - [x] Issue 2: Checkout integrado web de tarjetas de crédito con Stripe Elements.
  - [x] Issue 3: Integración del SDK Móvil de Stripe (Expo / React Native) con soporte PaymentSheet y simulador seguro de cobro. Reorganizado el layout móvil para desacoplar completamente la membresía Premium y Stripe de la pantalla de "Configuración de Seguridad", trasladando todo el flujo de suscripción y simulación de Stripe a la pestaña principal de Ajustes (activeTab === 'more').

- [x] **FASE 11: Migración del Frontend Mobile a Kotlin Nativo**
  - [x] Etapa 1: Infraestructura Base y Navegación. Inicializado el proyecto nativo en `android-native/` con Gradle Kotlin DSL, dependencias (Compose, Hilt, Retrofit, Room, DataStore, Google Maps), tema visual oscuro premium, cliente API con interceptor de sesión Sanctum, y barra de navegación inferior (Panel, Mapa, Familia, Ajustes) con Compose Navigation.
  - [x] Etapa 2: Flujo de Autenticación y Verificación OTP. Creados los modelos de datos, servicio API de Retrofit, repositorio con parseo de errores de Laravel, ViewModels de estado para Login, Registro y Verificación, e implementadas las pantallas nativas (Login con visualizador de contraseña, Registro con validación E.164 de teléfono y Verificación OTP) integradas con redirección en NavGraph.
  - [x] Etapa 3: Módulo de Bienestar (Tab 1: Panel & Configuración). Creado el cliente de red (CheckIn, Contacts, Settings), implementados sus repositorios vinculados por Hilt, desarrollados los ViewModels (PanelViewModel y AjustesViewModel), y reconstruidas las vistas nativas: Panel (con banner dinámico de estado, botón Estoy OK con spinner y listado histórico de reportes por origen) y Ajustes (con formulario de perfil, intervalo de reporte, Modo Sueño con zona horaria automática, webhook de SMS/WhatsApp, auto-check-in por Wi-Fi/podómetro y CRUD de contactos con reordenación secuencial).
  - [x] Etapa 4: Tracking de Ubicación y Cola Offline (Tab 2: Mapa). Desarrollado el cliente de red (Location y Circles), base de datos Room para cola local y dao, inyección de DB por Hilt, repositorio con guardado offline en Room ante fallos de red y vaciado ordenado de la cola de subidas, CoroutineWorker de WorkManager para sincronización automática al recuperar red, e implementado el Foreground Service (TrackingService) with FusedLocationProviderClient para rastreo persistente y lectura de batería/red. Reconstruida la vista nativa de Mapa (Google Maps Compose) integrando badges de sensores (GPS desactivado, Sin Señal, Conduciendo) y botón de redirección Maps nativo.
  - [x] Etapa 5: Conducción y Detección de Accidentes (Acelerómetro). Implementada histéresis de conducción en segundo plano (5s en debug, 1m/2m en producción), activando el acelerómetro en coche. Desarrollado el algoritmo de detección vectorial ($a \ge 4.5$G) seguido de 3 segundos de inmovilidad total por varianza. Creada la actividad de pre-alerta a pantalla completa (`CrashAlertActivity`) con sirena sonora en bucle y cuenta regresiva de 15s, conectada a la subida API `/alerts/crash`, aceleración GPS a 5s y canal de respaldo local SMS automático. Registrado en el manifiesto.
  - [x] Etapa 6: Alertas Críticas (SOS) y Geocercas Dinámicas. Desarrollados los DTOs, interfaz de red, Hilt DI y repositorio para SOS. Creado el módulo nativo `AudioRecorder` que encapsula la grabación silenciosa de 15s de audio ambiente a caché en formato AAC. Integrado el botón SOS en el Header de `PanelScreen` con pulsación larga de 3s (Toast explicativo en clicks simples), gatillando la subida a Laravel, acelerando a 5s el GPS en segundo plano, grabando y subiendo el audio mediante multipart, y enviando SMS local si no hay red.
  - [x] Etapa 7: Gestión de Núcleos y Stripe SDK. Creados DTOs, interfaces Retrofit y repositorios Hilt para Subscripciones de pasarela de pago. Desarrollado `FamiliaViewModel` para soportar las llamadas CRUD de círculos y subscripción Stripe. Implementada la vista nativa [FamiliaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/FamiliaScreen.kt) con selector dropdown de núcleos, listado de miembros por roles con control de expulsión/abandono, copia de código de invitación al portapapeles, formularios de creación/unión, y el panel de Stripe Premium con pasarela redireccionable nativa.
  - [x] Etapa 8: Re-organización de Navegación y Nuevas Pestañas.
    - [x] Re-habilitada la pestaña "Estoy ok" (anteriormente "Panel") en la barra de navegación inferior.
    - [x] Agregada la pestaña "Vehículo" para telemetría vehicular y límites de velocidad.
    - [x] Agregada la pestaña "Premium" dedicada a los planes de suscripción Stripe/MercadoPago/PayPal.
    - [x] Mantenido el botón flotante superior izquierdo para acceder a la pantalla de Ajustes desvinculada.

### In Progress:
- [ ] **FASE 12: Configuración de Entornos de Despliegue y Validación Final**
- [ ] **FASE 13: Depreciación de Web Funcional y Enfoque Móvil Exclusivo** (Plan de Trabajo en [plan_depreciacion_web.md](file:///home/usuario/aplicaciones/estoyok/docs/plan_depreciacion_web.md))

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
