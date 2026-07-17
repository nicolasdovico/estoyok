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
  - [x] Etapa 4: Tracking de Ubicación y Cola Offline (Tab 2: Mapa). Desarrollado el cliente de red (Location y Circles), base de datos Room para cola local y dao, inyección de DB por Hilt, repositorio con guardado offline en Room ante fallos de red y vaciado ordenado de la cola de subidas, CoroutineWorker de WorkManager para sincronización automática al recuperar red, e implementado el Foreground Service (TrackingService) with FusedLocationProviderClient para rastreo persistente y lectura de batería/red. Reconstruida la vista nativa de Mapa (Google Maps Compose) integrando badges de sensores (GPS desactivado, Sin Señal, Conduciendo) y botón de redirección Maps nativo. Implementada la depuración y resolución de pérdida de rastreo de fondo mediante logs detallados, vaciado automático (auto-flush) de la cola de subidas local Room al recibir reportes online exitosos, y diálogos de advertencia prominente ("Prominent Disclosure") en MapaScreen y AjustesScreen que asisten al usuario para activar el permiso "Permitir todo el tiempo" (ACCESS_BACKGROUND_LOCATION) en el sistema.
  - [x] Etapa 5: Conducción y Detección de Accidentes (Acelerómetro). Implementada histéresis de conducción en segundo plano (5s en debug, 1m/2m en producción), activando el acelerómetro en coche. Desarrollado el algoritmo de detección vectorial ($a \ge 4.5$G) seguido de 3 segundos de inmovilidad total por varianza. Creada la actividad de pre-alerta a pantalla completa (`CrashAlertActivity`) con sirena sonora en bucle y cuenta regresiva de 15s, conectada a la subida API `/alerts/crash`, aceleración GPS a 5s y canal de respaldo local SMS automático. Registrado en el manifiesto.
  - [x] Etapa 6: Alertas Críticas (SOS) y Geocercas Dinámicas. Desarrollados los DTOs, interfaz de red, Hilt DI y repositorio para SOS. Creado el módulo nativo `AudioRecorder` que encapsula la grabación silenciosa de 15s de audio ambiente a caché en formato AAC. Integrado el botón SOS en el Header de `PanelScreen` con pulsación larga de 3s (Toast explicativo en clicks simples), gatillando la subida a Laravel, acelerando a 5s el GPS en segundo plano, grabando y subiendo el audio mediante multipart, y enviando SMS local si no hay red.
  - [x] Etapa 7: Gestión de Núcleos y Stripe SDK. Creados DTOs, interfaces Retrofit y repositorios Hilt para Subscripciones de pasarela de pago. Desarrollado `FamiliaViewModel` para soportar las llamadas CRUD de círculos y subscripción Stripe. Implementada la vista nativa [FamiliaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/FamiliaScreen.kt) con selector dropdown de núcleos, listado de miembros por roles con control de expulsión/abandono, copia de código de invitación al portapapeles, formularios de creación/unión, y el panel de Stripe Premium con pasarela redireccionable nativa.
  - [x] Etapa 8: Re-organización de Navegación y Nuevas Pestañas.
    - [x] Re-habilitada la pestaña "Estoy ok" (anteriormente "Panel") en la barra de navegación inferior.
    - [x] Agregada la pestaña "Vehículo" para telemetría vehicular y límites de velocidad.
    - [x] Agregada la pestaña "Premium" dedicada a los planes de suscripción Stripe/MercadoPago/PayPal.
    - [x] Mantenido el botón flotante superior izquierdo para acceder a la pantalla de Ajustes desvinculada.
    - [x] Rediseñado el selector superior de núcleos en `MapaScreen.kt` a un formato "combo" píldora centrado, agregando opciones directas para Crear, Unirse y Administrar núcleos (Life360 style).
    - [x] Ocultada la opción "Núcleo" de la barra de navegación inferior en ambas plataformas, re-organizando el menú inferior a 4 ítems principales (Mapa, Vehículo, Estoy ok, Premium).
    - [x] Implementada la verificación y solicitud reactiva de permisos de Geolocalización y Notificaciones en `MapaScreen.kt` antes de levantar el Foreground Service, evitando cierres inesperados (crashes) de la app por falta de permisos.
    - [x] Desarrollado el panel deslizable inferior (BottomSheet/Expandable card) interactivo en `MapaScreen.kt` (Life360 style) que colapsa mostrando solo un miembro (o el seleccionado) y se expande mediante swipe vertical revelando la lista vertical completa (`LazyColumn`).
    - [x] Desarrollado el encuadre automático e inteligente de la cámara en `MapaScreen.kt` mediante `LatLngBounds` y `CameraUpdateFactory`, que auto-enfoca y aleja (zoom out) el mapa para englobar a todos los familiares del núcleo cuando se abre la app o se cambia de núcleo. Se integró un botón flotante circular de enfoque manual rápido.
    - [x] Desactivado el botón de ubicación nativo duplicado de Google Maps (color violeta) y configurado un `contentPadding` de `120.dp` en el mapa para desplazar de forma prolija las marcas de agua de Google por encima de nuestro panel deslizable.
    - [x] Configurada la constante `BASE_URL` de la app nativa Kotlin en `NetworkModule.kt` apuntando al host local `http://127.0.0.1:8000/api/` para desarrollo y depuración local por cable USB (mediante reenvío de puertos `adb reverse`).
    - [x] Diseñado e implementado el selector dinámico segmentado de servidores en `LoginScreen.kt` (**💻 Local (USB)** y **🌐 Railway**) persistido en DataStore, el cual permite alternar en caliente la URL base del backend sin requerir nuevas compilaciones de la app.
    - [x] Corregida la función `clearSession()` en `SessionManager.kt` para que no borre la selección del servidor (reemplazando `clear()` general por remociones de claves individuales de sesión), evitando que la app vuelva a Local (USB) al expirar la sesión.
    - [x] Descargada e integrada la tipografía premium **Outfit** (Regular, Medium, SemiBold, Bold) en los recursos de la app (`res/font/`) y configurada como la tipografía oficial del sistema de diseño en `Type.kt` para todos los componentes visuales nativos.
    - [x] Implementado el interceptor de errores `401 Unauthorized` en `AuthInterceptor.kt` para limpiar automáticamente la sesión local mediante DataStore y redirigir de inmediato al usuario a la pantalla de Login si el token expira o es revocado.
    - [x] Corregida la actualización en tiempo real de los marcadores (chinches) de ubicación de familiares en `MapaScreen.kt` mediante el uso de Compose keys y `LaunchedEffect`, permitiendo ver el desplazamiento continuo de los miembros en el mapa sin tener que cambiar de pestaña.
    - [x] Creada la guía de compilación de Android nativo desde línea de comandos (CLI) en [compilacion_android_cli.md](file:///home/usuario/aplicaciones/estoyok/docs/compilacion_android_cli.md).
    - [x] Implementada la personalización de marcadores de familiares en el mapa nativo ([MapaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/MapaScreen.kt)), reemplazando los chinches estándar por avatares circulares que cargan su foto de perfil (vía Coil) o muestran sus iniciales si no tienen imagen, con bordes de estado de conexión.
    - [x] Corregidas múltiples advertencias de compilación y elementos obsoletos (deprecados) en los archivos [NavGraph.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/navigation/NavGraph.kt), [MapaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/MapaScreen.kt), [AjustesScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt) y [FamiliaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/FamiliaScreen.kt).
    - [x] Implementado el flujo completo de Detalle de Miembros y Recorridos Históricos (Nativo & Backend), permitiendo la subida de fotos de perfil (avatar) de cada usuario (endpoint `POST /api/settings/avatar` + almacenamiento local público), y la visualización de rutas pasadas mediante un selector de fecha en un `ModalBottomSheet`, aplicando restricciones de plan (24h para usuarios gratuitos con promo premium y 30 días para Premium) y renderizado de `Polyline` con auto-ajuste de cámara en el mapa.
    - [x] Implementada la segmentación inteligente de recorridos por viaje (Opción A) en `MapaScreen.kt`. Las ubicaciones diarias se agrupan en sub-viajes por brechas de tiempo/distancia, calculando métricas (distancia Haversine, duración, velocidad máx) y mostrándolas como tarjetas interactivas en el `ModalBottomSheet` con redirección fluida al mapa a pantalla completa y controles de navegación ("Atrás" / "Salir").
    - [x] Corregidos problemas de usabilidad, visualización y cálculos en el historial de recorridos nativo en `MapaScreen.kt`:
      - [x] Habilitado el scroll vertical en el `ModalBottomSheet` mediante el modificador `.verticalScroll(rememberScrollState())` en la `Column` contenedora, permitiendo ver listados con gran cantidad de viajes detectados (ej: de 7 a 10 viajes).
      - [x] Invertido el orden de renderizado del listado de viajes para mostrar el viaje más reciente arriba de todo (`segments.reversed().forEach`).
      - [x] Corregido el bug del parseo de fechas de base de datos (`yyyy-MM-dd HH:mm:ss`) en `parseIsoToSeconds` y `formatTime`, solucionando el problema de la duración fija en "1 min" y permitiendo mostrar la hora real de inicio/fin de cada viaje.
      - [x] Mejorada la precisión del texto de duración calculando de forma detallada la diferencia de segundos (formateando a `"Menos de 1 min"`, `"X min"` y `"X h Y min"`).
      - [x] Rediseñado el carrusel de fechas de historial para usuarios gratuitos: ahora siempre se visualiza el día calendario y mes correspondiente (ej. `"10 Jul 🔒"`), permitiendo identificar claramente la fecha en lugar de las etiquetas genéricas `"🔒 PRO"`.
      - [x] Implementado el módulo de Zonas Seguras (Geofences) en la aplicación nativa (Kotlin):
        - [x] CRUD completo de Zonas Seguras en repositorio, ViewModels y pantallas nativas. Se implementó la edición (PUT `/api/geofences/{id}`) mediante un botón con icono de lápiz (✏️) en el listado y un diálogo modal interactivo para modificar el nombre, radio y asignación a miembros.
        - [x] Listado de Zonas Seguras en el panel deslizable de la home, permitiendo a los creadores/administradores editarlas o eliminarlas directamente.
        - [x] Integración de Zonas Seguras en la línea de tiempo (timeline) del historial de recorridos, intercalando de forma inteligente y cronológica las permanencias/estadías del familiar entre cada viaje del día.
      - [x] Rediseño completo de la interfaz de la aplicación nativa en base al layout de Life360 y utilizando la nueva paleta de color **Electric Turquoise** (`#00E5D9` / `#0D9488`):
        - [x] Actualización global de colores de marca primarios y secundarios en `Color.kt` y `Theme.kt`. Se ajustó la firma del color del primario original (`#00F0C0`) al turquesa eléctrico (`#00E5D9`) para asegurar su correcta percepción azulada (no verde) en pantallas físicas.
        - [x] Rediseño de controles flotantes sobre el mapa en `MapaScreen.kt`: botón de Ajustes (engranaje) en la parte superior izquierda en formato circular con sombreado y bordes M3.
        - [x] Removidos los botones flotantes redundantes de "Estoy OK" y "SOS" sobre el mapa para mantener la interfaz limpia y evitar saturación visual, dejando estas acciones en la pestaña principal dedicada.
        - [x] Ajustado el zoom inicial y centrado automático del mapa sobre los miembros en `MapaScreen.kt` de `15f` (demasiado cercano) a un nivel de zoom de **`13.5f`**, logrando una visualización más amplia y cómoda del entorno.
        - [x] Resuelta la superposición visual en la esquina superior izquierda de `MapaScreen.kt` configurando `compassEnabled = false` y `mapToolbarEnabled = false` en las directivas de `MapUiSettings` de Google Maps, evitando que la brújula y botones por defecto del SDK se rendericen detrás de nuestro botón de Ajustes.
      - [x] Mejoras en el Historial de Recorridos y Estadías (Estilo Life360):
        - [x] Backend: Retorno de columnas `speed` e `is_driving` en consultas del historial de ubicaciones en `/api/circles/{circle}/members/{member}/history`.
        - [x] App Nativa: Actualización de `LocationHistoryDto` para mapear los nuevos campos de velocidad y conducción.
        - [x] App Nativa: Implementación del componente Compose reactivo `AddressText` con `Geocoder` nativo de Android en segundo plano para resolución asíncrona y gratuita (costo cero) de nombres de calles y lugares.
        - [x] App Nativa: Algoritmo de clasificación de modo de transporte local (Caminata 🚶, Bicicleta 🚲, Vehículo 🚗) según velocidad y flag de conducción.
        - [x] App Nativa: Algoritmo de detección inteligente de estadías generales e intercalado automático de permanencias (mayores a 5 minutos) en el historial de rutas diarias.
        - [x] Rediseño visual premium de las tarjetas de historial en el panel de detalles de miembros (estilo Life360 con iconos de gran tamaño, flecha de trayecto `Origen → Destino` resuelta por geolocalización, franjas horarias y badges de velocidad).
        - [x] Filtro de micro-movimientos: Eliminación de caminatas internas a pie menores a 150m en `segmentHistoryPoints` y fusión automática de tiempos en estadías continuas.
        - [x] Estadía de día completo: Si el usuario no registra desplazamientos pero tiene coordenadas en el día, se muestra una estadía unificada de "Todo el día" (00:00 - 23:59).
        - [x] Algoritmo de unión de trayectos continuos: Rediseñado el segmentador de viajes para evitar que brechas GPS cortas (menores a 5-10 minutos) o distancias vehiculares fragmenten un viaje continuo, aplicando reglas de velocidad y parada física (estadía menor a 200m).
        - [x] Tracking dinámico y adaptativo: Implementada la política de Life360 en `TrackingService.kt`, ajustando dinámicamente intervalos de muestreo y distancias mínimas (10s/40m en auto, 30s/15m caminando y 5m/20m en estadía) con histéresis de 2 minutos para evitar oscilaciones en semáforos, logrando alta fidelidad online y mínimo impacto de batería.
        - [x] Creación de Zona Segura desde el Historial: Incorporado un botón de escudo (🛡️) en las tarjetas de estadía no registradas dentro de la línea de tiempo. Al pulsarlo, abre el diálogo de creación de Zona Segura pre-configurado con las coordenadas exactas de esa permanencia.
        - [x] Atajo de Edición de Zona Segura en Historial: Se añadió un botón con icono de lápiz (✏️) en las tarjetas de estadía que ya son zonas seguras registradas, restringido mediante validación local únicamente al Administrador (Owner) del Núcleo, permitiendo su edición rápida.
        - [x] Ajuste de Zoom Barrial en Mapa: Modificado el nivel de zoom por defecto al enfocar un miembro de `13.5f` a **`15.5f`** (logrando la visualización detallada de calles y manzanas de `zoom.jpg`). Implementada regla de proximidad para evitar acercamiento extremo de `LatLngBounds` when los familiares están juntos (menos de 1 km), fijando el zoom en `15.5f`.
        - [x] Unificación de Botón de Ajustes en Mapa: Eliminado el botón flotante local de Ajustes (⚙️) redundante en `MapaScreen.kt`, dejando únicamente el botón global de `NavGraph.kt` para evitar la duplicación/superposición visual y mantener un comportamiento coherente de navegación.
        - [x] Estadía en Curso Activa ("Ahora"): Implementada lógica en `buildHistoryTimeline` para detectar si el día consultado es hoy. En ese caso, la última permanencia muestra como hora de fin **"Ahora"** y la duración se calcula dinámicamente hasta el momento presente (ej: *"Desde hace 2 h 15 min"*).
        - [x] Badges de Movimiento sobre Avatares: Incorporados pequeños círculos flotantes de estado (Status Badges) en la esquina inferior derecha de las fotos/iniciales de los miembros. Clasifica en tiempo real según la telemetría: Vehículo (🚗), Bicicleta (🚲) y Caminando (🚶), tanto en el mapa (`MarkerComposable`) como en la lista de familiares (`MemberRowItem`).
        - [x] Backend: Creado el endpoint `GET /api/circles/{circle}/members/{member}/drives` en `DriveController.php` para obtener trayectos finalizados y calcular en caliente distancia (Haversine), score de seguridad, excesos de velocidad, aceleraciones rápidas y frenadas bruscas. Limita a un trayecto preview si el usuario actual es de plan gratuito (Free), o hasta 30 si es Premium. Integrada suite de tests completa (`DriveReportsTest.php`) con cobertura total.
        - [x] Corrección de Anclaje de Marcadores en Mapa: Reubicado el nombre del miembro en la parte superior y posicionado el puntero físico (piquito rotado) en la base inferior absoluta del marcador de mapa (`MarkerComposable`). Esto hace que el anclaje predeterminado de Google Maps `(bottom-center)` apunte de forma exacta sobre la calle del GPS.
        - [x] Seguimiento Online Continuo (Animación de Marcadores): Implementado el movimiento fluido y continuo de los marcadores en el mapa cuando cambian las coordenadas de ubicación de los miembros en tiempo real, reemplazando los saltos abruptos por transiciones animadas de deslizamiento (1.5 segundos) tanto en la App Móvil Nativa (Jetpack Compose Google Maps) como en la Plataforma Web (React Leaflet).
        - [x] Rediseño de Zonas Seguras en Mapa: Eliminadas las chinches (marcadores/pines de Google Maps) de las Zonas Seguras en el mapa para reducir la saturación visual, representándolas únicamente a través de los círculos perimetrales translúcidos pintados en el color Turquesa Eléctrico (`#00E5D9`) de Estoy Ok (tanto en Mobile como en Web).
        - [x] Estabilización de Insignias de Movimiento (Badges de Tránsito): Corregido el problema por el cual no se visualizaban los iconos de estado de movimiento (🚗/🚶) sobre los avatares en el mapa y la lista. Se rediseñó el contenedor de avatar para usar tamaños fijos simétricos que previenen el recorte en el canvas de Google Maps y se validó que las insignias se muestren según los umbrales de movimiento activos (caminata 🚶 $\ge 1.5$ km/h, bicicleta 🚲 $\ge 5$ km/h, vehículo 🚗/conducción $\ge 15$ km/h), ocultando por completo la insignia (retorna `null`) si el usuario está quieto.
        - [x] Telemetría y Estadía en Marcadores de Mapa: Agregada información contextual reducida (`7.5.sp`) debajo del nombre en la etiqueta del marcador de cada miembro: si está conduciendo/en auto, muestra su velocidad actual (ej. `65 km/h`); si está quieto (sin movimiento), calcula y muestra la duración de su estadía en ese sitio (ej: `10 min`, `1h 45m`) mediante un tracker de permanencias local (`stayTracker`) basado en la fórmula Haversine.
        - [x] Reubicación del Toggle de Rastreo y Auto-Inicio: Removido el interruptor ("Rastreo On/Off") de la pantalla del mapa (`MapaScreen.kt`) para limpiar y desaturar la interfaz visual. El control se trasladó a la pantalla de Ajustes (`AjustesScreen.kt` / `AjustesViewModel.kt`) bajo la tarjeta "Ubicación en Tiempo Real". Asimismo, se configuró un auto-inicio por defecto (`LaunchedEffect` al iniciar el mapa): si los permisos de GPS y notificaciones están concedidos, el servicio de rastreo en segundo plano se activa automáticamente.
        - [x] Despliegue Automático de Recorridos en Click de Marcador: Modificado el callback `onClick` de los marcadores (`MarkerComposable`) de los miembros en el mapa. Al hacer click sobre el pin o nombre, se selecciona el familiar en el viewmodel y se despliega de forma inmediata el panel de detalles (`ModalBottomSheet`). Además, se agregó un `LaunchedEffect` en dicho panel para auto-cargar de forma asíncrona la ruta de hoy ("Hoy"), eliminando pasos intermedios.
        - [x] Corrección en Edición de Avatar y Control de Tamaño (2MB): Solucionado el problema de almacenamiento en caché en Coil (`AsyncImage`) implementando un sistema de versionado local (`avatarVersion` en el ViewModel) que se concatena al URL de la foto (`?v=1`) al completar la subida. Además, se añadió una validación local instantánea del tamaño del archivo seleccionado: si el archivo supera los 2 MB, se muestra un Toast amistoso informando al usuario en vez de fallar a nivel de red/servidor.
        - [x] Eliminación de Nombre Redundante en Marcador de Mapa: Removido el nombre de pila del usuario de la tarjeta flotante superior en `MarkerComposable`. El marcador ahora muestra únicamente la foto de perfil o iniciales del miembro (evitando redundancias y saturación visual). La tarjeta flotante superior se conserva única y exclusivamente para mostrar la telemetría en tiempo real (velocidad 🚗 o duración de estadía 🚶) cuando se encuentra activa.
        - [x] Consistencia de Avatares en Pantalla de Vehículo: Modificado el componente `AsyncImage` en el selector horizontal de familiares de la solapa Vehículo (`VehiculoScreen.kt`) para concatenar el cache-buster versionado (`?v=version`) al URL del avatar del usuario actual. Esto soluciona los problemas de almacenamiento en caché y permite ver la foto actualizada o iniciales del usuario al instante.
        - [x] Robustez y Fallback de Iniciales en Carga de Avatar: Corregido el problema de visualización de avatares en entornos con datos desincronizados (como la base de datos de Railway vs Local). Se reemplazaron las llamadas tradicionales de `AsyncImage` por `SubcomposeAsyncImage` en las vistas de Mapa, Lista de Familiares, Hoja de Detalles y Selector de Vehículo. Ahora, ante cualquier fallo de red (404 por imágenes no migradas, URLs locales rotas o valores `"null"` residuales), el sistema muestra automáticamente las iniciales del usuario calculadas localmente en la función `error` de Coil, en lugar de renderizar un círculo vacío.
        - [x] Backend: Resolución Dinámica de URLs de Avatares (Múltiples Entornos): Modificado el método accesor `getAvatarUrlAttribute` del modelo `User.php` para resolver la URL absoluta utilizando el esquema y host de la petición HTTP actual (`request()->schemeAndHttpHost()`) cuando haya un contexto de request activo. Para evitar que la terminación SSL de proxies inversos (como el de Railway) forzara la URL a un protocolo inseguro `http://` (bloqueado por la política Cleartext Traffic de Android), se añadió soporte explícito para detectar la cabecera `X-Forwarded-Proto`.
        - [x] Carga Fluida de Iniciales (Loading State): Configurado el parámetro `loading` en los componentes `SubcomposeAsyncImage` en listas y hojas de detalle para mostrar las iniciales del usuario durante la carga de red.
        - [x] Corrección de Renderizado de Avatares en Marcador de Mapa (Pre-descarga a Bitmap en Screen Level y Concurrencia): Solucionado el problema de la no-visualización de avatares sobre los marcadores del mapa. Dado que `MarkerComposable` se ejecuta en un árbol de composición separado (gestionado por la API nativa de Google Maps) donde los ciclos de vida y corrutinas de `LaunchedEffect` locales son cancelados o ignorados, se trasladó la lógica de pre-descarga al nivel superior de la pantalla (`MapaScreen.kt`). Las imágenes se descargan de forma asíncrona mediante un mapa observable indexado por ID de miembro (`markerBitmaps`) y se inyectan ya decodificadas como objetos `Bitmap` directamente en el marcador. Se reemplazó el casteo directo a `BitmapDrawable` por la llamada universal `drawable.toBitmap()` de `core-ktx` para soportar correctamente los drawables empaquetados en transiciones de carga de Coil (como `CrossfadeDrawable`). Adicionalmente, se configuró el bucle de descarga para lanzar corrutinas asíncronas en paralelo (`launch { }`) para cada miembro; esto evita que las URLs de localhost inactivas o rotas (comunes en bases de datos de prueba) bloqueen secuencialmente la descarga de los avatares activos del resto del grupo.
        - [x] Backend: Configuración de Enlaces Simbólicos Relativos en Railway: Corregido el problema por el cual los accesos a las imágenes de avatars subidos en producción daban 404 debido a que `php artisan storage:link` crea enlaces simbólicos absolutos (que apuntaban a rutas de la etapa de construcción, `/workspace/...`, inexistentes en la etapa de ejecución, `/app/...`). Se modificó `railway.json` para eliminar enlaces obsoletos y forzar el uso de `--relative`, y se agregó el paquete de Composer `symfony/filesystem` requerido por Laravel para soportar enlaces simbólicos relativos. Para asegurar que el enlace simbólico persista en el contenedor de ejecución activo (ya que preDeployCommand se corre en contenedores temporales de ciclo corto), se trasladó la creación del enlace simbólico al inicio del `startCommand`.
        - [x] Estabilización de Rastreo e Inmovilidad (Android Native): Implementados filtros de precisión (>40m) y desplazamiento de deriva (<15m a <3km/h) en `processLocationUpdate` de `TrackingService.kt` para evitar que el marcador rebote o salte en el mapa cuando el usuario está quieto o bajo techo.
        - [x] Auto-inicio de Rastreo en Segundo Plano (Android Native): Desarrollada la clase `BootReceiver.kt` y registrada en `AndroidManifest.xml` para auto-iniciar el rastreo en segundo plano tras reiniciar el teléfono o actualizar la app. Persistido el estado del switch de rastreo (`TRACKING_ENABLED`) en `SessionManager.kt` e inyectado en `AjustesViewModel.kt` y `MapaViewModel.kt` para sincronizar y respetar la preferencia elegida de forma reactiva.
        - [x] Exclusión de Optimización de Batería (Android Native): Declarado el permiso `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` y añadido diálogo de advertencia prominente en `MapaScreen.kt` para solicitar al usuario configurar el uso de batería de la app como "Sin Restricciones" (evitando la suspensión de servicios en segundo plano por el sistema operativo).
        - [x] Sincronización Programada (Android Native): Agendado y configurado el worker de sincronización `LocationSyncWorker` en `EstoyOkApplication.kt` con WorkManager utilizando restricciones de red conectada (`NetworkType.CONNECTED`), garantizando el vaciado automático de la cola Room local al recuperar la red.
        - [x] Resiliencia ante Cierre Forzado (Android Native): Sobreescrito el método `onTaskRemoved` en `TrackingService.kt` para programar un reinicio del servicio mediante `AlarmManager` en 5 segundos si el usuario desliza y fuerza el cierre de la app desde la pantalla de recientes.
        - [x] Sensor de Movimiento Significativo (Android Native): Integrado el sensor de hardware `Sensor.TYPE_SIGNIFICANT_MOTION` en `TrackingService.kt` para despertar y acelerar instantáneamente el GPS de su estado de reposo lento (Estadía) a modo activo en cuanto se detecta movimiento, optimizando la batería sin sacrificar precisión al iniciar trayectos.
        - [x] Dispersión de Marcadores Superpuestos (Android Native): Implementado el algoritmo de Dispersión Circular Dinámica (Spiderfying) en `MapaScreen.kt`. Al detectar familiares que se encuentran a menos de 20 metros de distancia física, se calcula un desfase geométrico circular (de aproximadamente 15 metros) para sus coordenadas de renderizado temporal en el mapa, permitiendo visualizar y pulsar cada avatar por separado cuando están en el mismo lugar (estilo Life360).
        - [x] Rediseño de Historial y Promoción Premium (Android Native): Reemplazada la cuadrícula de 30 días con candados para usuarios Free en `MapaScreen.kt` por una tarjeta de promoción Premium interactiva (CTA) que enlaza a la pantalla de planes. La interfaz de usuarios Free ahora muestra directamente la línea de tiempo de hoy sin elementos bloqueados distractores, mientras que los usuarios Premium conservan el selector completo de 30 días libre de candados.
        - [x] Rediseño de Marcadores en el Mapa (Android Native): Modificada la forma y tamaño de los marcadores de miembros sobre el mapa en `MapaScreen.kt` para coincidir con la estética premium de Life360. Se reemplazó el contenedor circular (`CircleShape`) por un cuadrado con vértices redondeados (`RoundedCornerShape(16.dp)`), se incrementó el tamaño de visualización del avatar (de `48.dp` a `56.dp`), el tamaño de la cola del puntero (de `14.dp` a `16.dp`) y el grosor del borde de estado a `3.5.dp`.
        - [x] Segmentación Semanal en Protección Vehicular (Android Native & Backend): Implementado el filtrado de trayectos por semanas mediante pestañas horizontales ("Semana actual", "Semana anterior", Semana N-2 y Semana N-3) en `VehiculoScreen.kt`. Modificado `DriveController.php` en el backend para permitir a todos los usuarios obtener los metadatos de los últimos 30 viajes para el cálculo de estadísticas semanales. Sin embargo, para usuarios Free, se remueven de forma segura los detalles de telemetría y coordenadas GPS (`route_points` y `events`) de los viajes históricos en la API, y el listado de trayectos de semanas pasadas se bloquea en la app móvil tras una tarjeta interactiva promocional Premium.





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
