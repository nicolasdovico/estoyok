# Bitácora de Resúmenes de Migración Kotlin (Android Nativo)

Este archivo consolida los resúmenes técnicos e hitos completados tras la finalización de cada etapa en la migración del frontend mobile de **Estoy Ok** a Kotlin nativo.

---

## 🟢 Etapa 1: Infraestructura Base y Navegación

### Resumen Técnico:
Se ha inicializado y estructurado el esqueleto del proyecto Android Nativo con arquitectura limpia por capas y un diseño visual coherente con la identidad oscura del producto.

### Componentes Creados e Integrados:
1. **Configuración de Gradle (Kotlin DSL):**
   * [settings.gradle.kts](file:///home/usuario/aplicaciones/estoyok/android-native/settings.gradle.kts): Define el módulo `:app` y repositorios.
   * [build.gradle.kts](file:///home/usuario/aplicaciones/estoyok/android-native/build.gradle.kts) principal y [app/build.gradle.kts](file:///home/usuario/aplicaciones/estoyok/android-native/app/build.gradle.kts): Configuran las dependencias de **Jetpack Compose, Dagger Hilt, Retrofit, Room, DataStore, Google Maps y Stripe SDK**.
   * [gradle.properties](file:///home/usuario/aplicaciones/estoyok/android-native/gradle.properties) y [gradle-wrapper.properties](file:///home/usuario/aplicaciones/estoyok/android-native/gradle/wrapper/gradle-wrapper.properties): Configuran las propiedades de la JVM y Gradle 8.5.

2. **Permisos y Manifiesto de Android:**
   * [AndroidManifest.xml](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/AndroidManifest.xml): Configura los permisos requeridos (ubicación en primer/segundo plano, sensores corporales, GPS, notificaciones push, grabación de audio para SOS y envío de SMS local).
   * Configurado tráfico HTTP en texto plano para el servidor local Docker (`10.0.2.2`).

3. **Sistema de Diseño Oscuro Premium:**
   * [Color.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/theme/Color.kt): Paleta de colores de marca (Emerald, Teal, Dark backgrounds).
   * [Type.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/theme/Type.kt) y [Theme.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/theme/Theme.kt): Estilos de texto y forzado de tema oscuro.

4. **Persistencia y Capa de Red:**
   * [SessionManager.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/data/local/SessionManager.kt): Basado en Android **DataStore Preferences** para guardar de forma reactiva el token de sesión.
   * [AuthInterceptor.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/data/remote/AuthInterceptor.kt): Inyecta el token Bearer en cada llamada de red.
   * [NetworkModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/di/NetworkModule.kt): Proveedores Hilt para Retrofit.

5. **Navegación e Interfaces Componibles:**
   * [Screen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/navigation/Screen.kt) y [NavGraph.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/navigation/NavGraph.kt): Manejan la barra de navegación inferior (`NavigationBar`) y Compose Navigation.
   * Placeholders de pantallas: [PanelScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt), [MapaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/MapaScreen.kt), [FamiliaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/FamiliaScreen.kt) y [AjustesScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt).
   * Inicialización global en [MainActivity.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/MainActivity.kt) y [EstoyOkApplication.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/EstoyOkApplication.kt).

---

## 🟢 Etapa 2: Flujo de Autenticación y Verificación OTP

### Resumen Técnico:
Se ha implementado de extremo a extremo la lógica de autenticación (Login, Registro y Verificación de correo con OTP de 6 dígitos) integrada con los endpoints de Laravel.

### Componentes Creados e Integrados:
1. **Modelos y DTOs de Datos:**
   * [AuthModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/data/model/AuthModels.kt): Modelos serializables con anotaciones `@SerializedName` para request/response de API de Laravel.
   * [Resource.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/util/Resource.kt): Envoltorio genérico para estados de carga, éxito y error.

2. **Capa de Red y Repositorio:**
   * [AuthApiService.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/data/remote/AuthApiService.kt): Endpoints HTTP (`/register`, `/login`, `/verify-email`, `/resend-otp`, `/logout`).
   * [AuthRepositoryImpl.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/data/repository/AuthRepositoryImpl.kt): Coordina el consumo del cliente API, la persistencia en DataStore y el parseo de respuestas de error de validación de Laravel (ej. leyendo el primer error del objeto `"errors"`).
   * [AuthModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/di/AuthModule.kt): Configura Hilt para vincular el repositorio.

3. **ViewModels y Vistas de UI (Jetpack Compose):**
   * [AuthViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/AuthViewModel.kt): Provee el estado reactivo `isAuthenticated` al Scaffold de la app para proteger rutas.
   * [LoginScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/login/LoginScreen.kt) y [LoginViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/login/LoginViewModel.kt): Login con visualización de contraseña mediante toggle del ojo.
   * [RegisterScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/register/RegisterScreen.kt) y [RegisterViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/register/RegisterViewModel.kt): Registro de usuario con validaciones de contraseñas y advertencia de prefijo `+` para el formato telefónico E.164.
   * [VerifyEmailScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/verify/VerifyEmailScreen.kt) y [VerifyEmailViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/auth/presentation/verify/VerifyEmailViewModel.kt): Pantalla para introducir los 6 dígitos numéricos recibidos por correo, con control de reenvíos.

4. **Navegación e Integración:**
   * [NavGraph.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/navigation/NavGraph.kt): Configurado con `LaunchedEffect(isAuthenticated)` para redirigir automáticamente al panel interno cuando el estado sea verificado y ocultar la barra inferior de navegación en las rutas de Login, Registro y Verificación.

---

## 🟢 Etapa 3: Módulo de Bienestar (Tab 1: Panel & Configuración)

### Resumen Técnico:
Se ha implementado en su totalidad el módulo de bienestar del usuario, compuesto por las pantallas de control de inactividad, registro manual de bienestar, historial cronológico de reportes con diferenciación de origen y la configuración completa de seguridad, automatización pasiva y contactos de emergencia con jerarquías.

### Componentes Creados e Integrados:
1. **Modelos y DTOs de Datos:**
   * [CheckInModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/data/model/CheckInModels.kt): Estructuras de datos para transacciones del endpoint `/check-in` y consulta de logs históricos `/check-ins`.
   * [ContactModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/data/model/ContactModels.kt): Modelos de representación de contactos de emergencia y reordenación de prioridades.
   * [SettingsModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/data/model/SettingsModels.kt): Estructuras de datos serializables para los distintos ajustes de configuración (intervalo de reportes, Modo Sueño, Twilio y automatizaciones pasivas).

2. **Capa de Red y Repositorios:**
   * `CheckInApiService.kt`, `EmergencyContactsApiService.kt` y `SettingsApiService.kt`: Endpoints de Retrofit mapeados con sus correspondientes cabeceras.
   * `CheckInRepositoryImpl.kt`, `EmergencyContactsRepositoryImpl.kt` y `SettingsRepositoryImpl.kt`: Capa de abstracción de datos para peticiones.
   * [WellbeingModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/di/WellbeingModule.kt): Configura Hilt para vincular todas las implementaciones de repositorios.

3. **ViewModels y Vistas de UI (Jetpack Compose):**
   * [PanelViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt): Controla el envío de check-in manual, la recarga de datos en segundo plano y el cálculo del cronómetro de expiración (adaptativo a nivel local en modo Debug/Local para tratar el intervalo como minutos en pruebas rápidas).
   * [AjustesViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesViewModel.kt): Maneja el guardado de Modo Sueño (pasando la zona horaria del dispositivo), toggle de Twilio, SSID de Wi-Fi, sensores físicos y operaciones CRUD/Reordenamiento de contactos de emergencia.
   * [PanelScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt):
     * **Banner de Estado:** Renderiza dinámicamente un banner temático ("Protegido y a Salvo", "Reporte Vencido", "Sin Reportes") según el temporizador del backend.
     * **Botón Estoy OK:** Botón circular con gradiente esmeralda y spinner interactivo.
     * **Historial:** Lista vertical con el desglose de reportes procesados detallando origen con emojis (manual 👆, wifi 📶, sensor 🚶, etc.).
   * [AjustesScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt): Panel unificado de configuraciones compuesto por:
     * Chips interactivos para seleccionar el intervalo de reporte (12h, 24h, 48h).
     * Panel de Modo Sueño con switches y inputs validados de horas.
     * Switch de reporte Twilio y switches de auto-check-in pasivo (SSID de Wi-Fi y acelerómetro).
      * CRUD de contactos con validador de número E.164, parentesco, correo opcional y flechas arriba/abajo de reordenación de prioridad en lista.
      * Botón de cierre de sesión conectado a la eliminación de credenciales en local.

---

## 🟢 Etapa 4: Tracking de Ubicación y Cola Offline (Tab 2: Mapa)

### Resumen Técnico:
Se ha implementado el módulo de rastreo satelital GPS activo en segundo plano, la persistencia/cola de ubicaciones en base de datos local (Room) en ausencia de red o caída del servidor, la sincronización automatizada (WorkManager) y el renderizado nativo del mapa (Google Maps Compose) integrando telemetrías y estados de sensores de los familiares.

### Componentes Creados e Integrados:
1. **Modelos y DTOs de Datos:**
   * [LocationModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/model/LocationModels.kt): DTOs para coordenadas de localización (`LocationUpdateRequest`, `LocationUpdateResponse`) y estado de sensores (`SensorStatusRequest`).
   * [CircleModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/model/CircleModels.kt): Modelos estructurados para mapear la lista de núcleos (`CircleDto`), miembros (`CircleMemberDto`) y posiciones de familiares.

2. **Base de Datos Room (Cola Offline):**
   * [OfflineLocationEntity.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/local/entity/OfflineLocationEntity.kt): Tabla `offline_locations` para el almacenamiento temporal de coordenadas en SQLite.
   * [OfflineLocationDao.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/local/dao/OfflineLocationDao.kt): Consultas SQLite para insertar, obtener ordenado cronológicamente y eliminar puntos.
   * [EstoyOkDatabase.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/data/local/EstoyOkDatabase.kt): Declaración base de la base de datos de la app.
   * [DatabaseModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/di/DatabaseModule.kt): Configuración Hilt para inyectar la instancia de Room y el DAO.

3. **Capa de Red, Repositorios e Hilos:**
   * `LocationApiService.kt` y `CircleApiService.kt`: Clientes HTTP de Retrofit.
   * `LocationRepositoryImpl.kt` y `CircleRepositoryImpl.kt`: Coordinadores de envío de datos y lectura de la cola local Room. Si hay pérdida de internet, guarda los registros automáticamente en local y detiene las subidas conservando el orden temporal.
   * [TrackingModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/di/TrackingModule.kt): Configura Hilt para inyectar los repositorios.

4. **Background Execution Services (Segundo Plano):**
   * [TrackingService.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/services/TrackingService.kt): Foreground Service nativo de Android. Genera un canal de notificación en barra de tareas ("tracking_service_channel") y captura coordenadas mediante `FusedLocationProviderClient`. Lee el nivel de batería (`BatteryManager`) e internet (`ConnectivityManager`) para componer el reporte de red, con aceleración del radar a 5s en caliente ante perimetrados activos.
   * [LocationSyncWorker.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/services/LocationSyncWorker.kt): Trabajador de fondo (WorkManager) para subir la cola local al recuperar internet.
   * [EstoyOkApplication.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/EstoyOkApplication.kt): Modificado para habilitar soporte Hilt en WorkManager.

5. **ViewModels y Vistas de UI (Jetpack Compose):**
   * [MapaViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/MapaViewModel.kt): Controla el polling periódico de 10s de miembros, expone el núcleo activo y maneja el encendido/apagado del servicio persistente de fondo.
   * [MapaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/MapaScreen.kt):
     * **Capa Google Map:** Integra Google Maps Compose y markers reactivos de cada miembro.
     * **Cabecera Flotante:** Toggle selector del servicio GPS y menú dropdown del núcleo familiar activo.
      * **Carrusel de Familiares:** Tarjetas inferiores horizontales con telemetría (batería warning, velocidad en coche) y badges de sensores (GPS desactivado, Sin Señal, Conduciendo).
      * **Cómo llegar:** Botón flotante que dispara un Intent hacia Google Maps oficial para iniciar navegación guiada al punto seleccionado.

---

## 🟢 Etapa 5: Conducción y Detección de Accidentes (Acelerómetro)

### Resumen Técnico:
Se ha implementado la lógica inteligente de trayectos de conducción y el algoritmo crítico de detección de accidentes (colisiones de alto impacto) analizando vectores de aceleración tridimensional post-impacto y su varianza física en inmovilidad en segundo plano mediante los sensores nativos del dispositivo (`SensorManager`). Creada e integrada la interfaz de cuenta regresiva (pre-alerta sonora) y protocolos automáticos de máxima emergencia.

### Componentes Creados e Integrados:
1. **Modelos y DTOs de Datos:**
   * [CrashModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/model/CrashModels.kt): DTOs para solicitudes de colisión `/alerts/crash` y falsas alarmas `/alerts/crash/{id}/false-alarm`.

2. **Capa de Red y Repositorios:**
   * `CrashApiService.kt`: Cliente HTTP de Retrofit.
   * `CrashRepositoryImpl.kt`: Capa de abstracción de datos para colisiones.
   * [TrackingModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/di/TrackingModule.kt): Configurado en Hilt para proveer los servicios de colisiones.

3. **Background Sensor Monitoring (Segundo Plano):**
   * [TrackingService.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/services/TrackingService.kt):
     * **Histéresis de Conducción:** Evalúa la velocidad reportada por el GPS. Si supera los 25 km/h por más de 1 minuto, entra en conducción (`isDriving = true`). Al descender de ese umbral por más de 2 minutos, finaliza conducción (tiempos adaptados a 5s/10s en modo debug para simplificar pruebas en local).
     * **Gestión de Sensores:** Registra/desregistra dinámicamente el listener del acelerómetro (`Sensor.TYPE_ACCELEROMETER`) según el estado de conducción para ahorrar batería.
     * **Algoritmo de Colisiones:** Evalúa vectores tridimensionales:
       $$\text{Acc. Neta} = \sqrt{x^2+y^2+z^2} \ge 4.5\text{G}$$
       Si se cumple, inicia una ventana de 3 segundos recogiendo varianza: si es $< 0.15$G, se confirma impacto con inmovilidad del dispositivo e inicia la actividad de alerta.

4. **Countdown Pre-alert View (UI y SMS):**
   * [CrashAlertActivity.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/CrashAlertActivity.kt):
     * **UI de Impacto:** Viento a pantalla completa en rojo de alerta con animaciones.
     * **Sirena Sonora:** Reproduce en bucle el tono de alarma nativo del dispositivo (`RingtoneManager.TYPE_ALARM`).
     * **Bloqueo:** Anula el botón de volver (`onBackPressed`) obligando al usuario a cancelar explícitamente.
     * **Cancelación:** Detiene la sirena y finaliza el proceso sin reportar si el usuario pulsa "Estoy bien".
     * **Protocolo de Emergencia Expirado (15s):** Envía la petición API de impacto, acelera la tasa de GPS a 5s en segundo plano y activa el canal de respaldo SMS local nativo a cada contacto de emergencia con las coordenadas exactas de la colisión.
     * **Manifiesto:** Registrada en [AndroidManifest.xml](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/AndroidManifest.xml).

---

## 🟢 Etapa 6: Alertas Críticas (SOS) y Geocercas Dinámicas

### Resumen Técnico:
Se ha implementado la funcionalidad nativa de Alerta SOS Silenciosa en caso de emergencia, junto con la grabación y transmisión en segundo plano de clips de audio ambiental, y la aceleración temporal del muestreo GPS. Además, se integró en la UI de bienestar la protección ante pulsaciones accidentales mediante eventos gestuales prolongados.

### Componentes Creados e Integrados:
1. **Modelos y DTOs de Datos:**
   * [SosModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/model/SosModels.kt): DTOs para solicitudes de inicio SOS y subida multipart de audios.

2. **Capa de Red y Repositorios:**
   * `SosApiService.kt`: Cliente HTTP de Retrofit que define llamadas multipart (`MultipartBody.Part`) para transferencias binarias.
   * `SosRepositoryImpl.kt`: Capa de abstracción de datos para reportar la alarma e iniciar las subidas.
   * [TrackingModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/di/TrackingModule.kt): Configurado en Hilt para proveer los servicios de SOS.

3. **Módulos y Utilidades de Hardware:**
   * [AudioRecorder.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/util/AudioRecorder.kt): Helper nativo que inicializa `MediaRecorder` de Android (MPEG_4/AAC) para capturar de forma silenciosa el sonido ambiental desde el micrófono del teléfono y retornarlo como un archivo temporal de caché.

4. **ViewModels y Vistas de UI (Jetpack Compose):**
   * [PanelViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt):
     * Dispara asíncronamente el SOS silencioso (`POST /emergency-alerts/sos`).
     * Al recibir la confirmación de la alerta, activa la grabación de audio de fondo durante 15 segundos y sube el archivo al servidor (`POST /emergency-alerts/{id}/audio`).
     * Acelera la tasa del GPS de `TrackingService` a 5 segundos de intervalo para seguimiento en tiempo real de alta precisión.
     * En caso de pérdida de conexión de red, ejecuta una subida offline por SMS nativo enviando un mensaje inmediato a todos los contactos de emergencia del usuario.
     * **Botón SOS de Cabecera:** Pill button rojo posicionado junto al botón Actualizar.
     * **Gestión de Gestos:** Utiliza `detectTapGestures` en `pointerInput`. El toque simple emite un Toast preventivo/explicativo indicando que la alerta requiere presión continua. El toque sostenido durante 3 segundos activa el protocolo de emergencia, evitando disparos involuntarios.

---

## 🟢 Etapa 7: Gestión de Núcleos y Stripe SDK

### Resumen Técnico:
Se ha implementado el módulo CRUD completo para la administración de núcleos familiares (círculos), el copiado seguro a portapapeles de códigos de invitación y las operaciones de abandono/expulsión de miembros en caliente. Asimismo, se integró el portal de facturación y pasarela de suscripciones a planes Premium conectando a los controladores de checkout de Stripe. Con esta etapa, la migración completa del Frontend Mobile de React Native a Kotlin Nativo queda finalizada con éxito.

### Componentes Creados e Integrados:
1. **Modelos y DTOs de Datos:**
   * [SubscriptionModels.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/data/model/SubscriptionModels.kt): DTOs para estructurar las solicitudes `/subscriptions/checkout` del backend.

2. **Capa de Red y Repositorios:**
   * `SubscriptionApiService.kt`: Mapea llamadas HTTP de Retrofit de suscripciones.
   * `SubscriptionRepositoryImpl.kt`: Capa de datos para coordinar peticiones de cobro.
   * [TrackingModule.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/di/TrackingModule.kt): Configurado en Hilt para proveer los servicios de subscripción.

3. **ViewModels y Vistas de UI (Jetpack Compose):**
   * [FamiliaViewModel.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/FamiliaViewModel.kt):
     * Carga el estado de suscripción del perfil y los núcleos de seguridad del usuario en paralelo.
     * Soporta flujos CRUD: creación (`createCircle`), unirse (`joinCircle`), expulsión (`removeMember`) y disolución de núcleos (`deleteCircle`).
     * Solicita al servidor las URL de checkout (`checkoutSubscription`) para Stripe, PayPal o MercadoPago.
   * [FamiliaScreen.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/FamiliaScreen.kt): Screen nativa Jetpack Compose compuesta por:
     * **Selector de Núcleos:** Dropdown flotante superior.
     * **Portapapeles:** Muestra el código de invitación del círculo activo con botón interactivo de copiado rápido mediante `ClipboardManager`.
     * **Lista de Miembros:** Detalla nombre y rol (Admin/Miembro). Los administradores ven un botón de expulsión (icono de tacho) y los miembros estándar el de abandonar (icono de salida).
     * **CRUD UI:** Formularios limpios con validación de longitud de caracteres para crear o unirse a círculos.
     * **Paywall Premium:** Tarjeta verde de alta fidelidad que resalta beneficios exclusivos. Permite seleccionar el medio de pago (Stripe, MercadoPago, PayPal) mediante chips y gatilla un Intent de navegación al navegador oficial para completar el cobro de forma segura y autorizada.
