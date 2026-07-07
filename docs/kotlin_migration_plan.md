# Plan de Migración de Frontend Mobile a Kotlin (Native Android)

Este documento detalla la estrategia y el plan por etapas para migrar la aplicación móvil de **Estoy Ok** de **Expo (React Native / TypeScript)** a **Kotlin (Android Nativo)** con **Jetpack Compose**.

---

## 🎯 Objetivos de la Migración
1. **Precisión y Fiabilidad de Fondo:** Resolver las restricciones y el ciclo de vida limitado de Expo en segundo plano mediante el uso de **Android Foreground Services** nativos para el rastreo y la detección de colisiones/velocidad.
2. **Optimización de Batería:** Control granular sobre los sensores y la frecuencia de actualización del GPS.
3. **Arquitectura Sólida:** Implementar una arquitectura limpia (**Clean Architecture + MVVM**) usando Jetpack Compose, Coroutines/Flow, Retrofit, Room y Dagger Hilt.
4. **Cero Regresiones:** Mantener total compatibilidad con la API de Laravel v12 y sus contratos establecidos en OpenAPI/Swagger.

---

## 🛠️ Equivalencias Tecnológicas (React Native vs. Kotlin)

| Funcionalidad / Biblioteca | React Native (Expo) | Android Nativo (Kotlin) |
| :--- | :--- | :--- |
| **UI Engine** | React Native Elements / Tailwind | **Jetpack Compose** |
| **Navegación** | Expo Router (Tabs / Stack) | **Compose Navigation** |
| **Redes / API** | Axios | **Retrofit 2** + **OkHttp 3** |
| **Estado Asíncrono** | React Context / useState / useEffect | **StateFlow / SharedFlow** + ViewModels |
| **Inyección de Dependencias** | N/A (Manual) | **Dagger Hilt** |
| **Persistencia Local** | AsyncStorage | **Room Database** (Tablas) & **DataStore** (Key-Value) |
| **Rastreo de Ubicación** | Expo Location (Background Task) | **FusedLocationProviderClient** + **Foreground Service** |
| **Detección de Caídas/Crash** | Expo Sensors (Accelerometer) | **SensorManager** (`Sensor.TYPE_ACCELEROMETER`) |
| **Monitoreo de Sensores** | Expo Battery / NetInfo | **ConnectivityManager** / **BatteryManager** / **LocationManager** |
| **Grabación de SOS** | expo-av | **MediaRecorder** |
| **Pasarela de Pago** | `@stripe/stripe-react-native` (PaymentSheet) | **Stripe Android SDK** (`com.stripe:stripe-android`) |
| **Programación de Tareas** | Expo TaskManager | **WorkManager** (PeriodicWorkRequest) |

---

## 🏗️ Arquitectura y Estructura de Paquetes Propuesta

Se utilizará una arquitectura limpia por capas (**Clean Architecture**) en una estructura de paquetes basada en características (*feature-by-feature*) para mantener el código desacoplado:

```text
com.estoyok.app/
│
├── core/                       # Utilidades globales, red y base de datos
│   ├── data/
│   │   ├── local/              # Room Database, DataStore (Sesión)
│   │   └── remote/             # Retrofit API Services, Interceptores (Auth)
│   └── theme/                  # Colores, Tipografías, Tema Oscuro/Claro
│
├── features/                   # Módulos de negocio (Wellbeing, Tracking, Auth)
│   ├── auth/                   # Login, Registro, OTP
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/       # Compose Screens & ViewModels
│   ├── wellbeing/              # Check-in, Historial, Quiet Hours, Contactos
│   ├── tracking/               # Mapa, Núcleo, Sensores, Historial de Rutas
│   └── billing/                # Stripe, Planes Premium, Web/Checkout
│
├── services/                   # Servicios del Sistema Android de Fondo
│   ├── TrackingService.kt      # Foreground Service (GPS, Acelerómetro, Velocidad)
│   └── SyncWorker.kt           # WorkManager para sincronización de cola offline
```

---

## 📅 Plan de Migración por Etapas

Para evitar confusiones con los archivos y garantizar un flujo ordenado, dividimos el trabajo en **7 etapas secuenciales**. 

> [!IMPORTANT]
> No se eliminará la carpeta `mobile/` actual hasta que la app nativa en Kotlin esté completamente operativa y validada. La nueva aplicación vivirá temporalmente en una carpeta paralela (ej. `android-native/`).

---

### 🟢 Etapa 1: Infraestructura Base y Navegación
* **Objetivo:** Configurar el proyecto de Android Studio, dependencias e inyección de dependencias, base de datos local y navegación base.
* **Tareas:**
  1. Inicializar el proyecto con Gradle (Kotlin DSL).
  2. Configurar dependencias clave: Compose, Hilt, Retrofit, Room, DataStore y Play Services.
  3. Crear el cliente de API con Retrofit (`HttpClient` con interceptor para adjuntar el token Sanctum `Authorization: Bearer <token>`).
  4. Configurar DataStore para almacenar y recuperar de forma segura el `auth_token` de sesión.
  5. Diseñar la estructura de navegación con **Compose Navigation** (4 pestañas inferiores: `Panel`, `Mapa`, `Familia`, `Ajustes`).

---

### 🟢 Etapa 2: Flujo de Autenticación y Verificación OTP
* **Objetivo:** Implementar el acceso de usuarios sincronizado con el backend Laravel.
* **Tareas:**
  1. **Pantalla de Login:** Inputs con validación en tiempo real de email, campo de contraseña con toggle visual (icono de ojo).
  2. **Pantalla de Registro:** Creación de cuenta, validación de campos.
  3. **Verificación OTP:** Pantalla con entrada de código de 6 dígitos que consume `/verify-email` y lógica para reenvío de OTP.
  4. Sincronización del estado de autenticación (si hay un token guardado en DataStore, redirigir directamente al Dashboard).

---

### 🟢 Etapa 3: Módulo de Bienestar (Tab 1: Panel & Configuración)
* **Objetivo:** Llevar toda la lógica de reportes de bienestar activos y pasivos.
* **Tareas:**
  1. **Dashboard de Bienestar:** Botón de "Estoy Ok" (Check-in manual).
  2. **Banner de Estado:** Reflejar reactivamente el estado actual ("Protegido y a Salvo", "Reporte Vencido", "Sin Reportes") con los colores del sistema de diseño original (Esmeralda/Rojo/Gris).
  3. **Historial de Reportes:** Listar los últimos 20 check-ins detallando el origen (`manual`, `wifi`, `movement`, `sms`, `whatsapp`).
  4. **Sección Contactos de Emergencia:** Listado con indicador de prioridad, flechas para reordenamiento interactivo que actualicen en base de datos local y consuman `/emergency-contacts/reorder`.
  5. **Modo Sueño y Ajustes:** Formulario validado con switches para Modo Sueño, inputs de horas formateados, toggles de automatización (Wi-Fi seguro y podómetro/sensor).

---

### 🟢 Etapa 4: Tracking de Ubicación y Cola Offline (Tab 2: Mapa)
* **Objetivo:** Implementar el rastreo satelital con soporte para fallos de red.
* **Tareas:**
  1. Crear un **Foreground Service** (`TrackingService`) que ejecute `FusedLocationProviderClient` con actualizaciones en segundo plano.
  2. **Cola de Ubicaciones Offline:** Si el servicio no tiene red (validado con `ConnectivityManager`), insertar las coordenadas en una base de datos local de Room.
  3. **SyncWorker:** Un trabajador de **WorkManager** programado para enviarse al recuperar conexión, realizando peticiones en lote a `/api/locations/update` para vaciar la cola.
  4. **Reporte de Sensores:** El servicio debe enviar de manera periódica (o en `/api/locations/sensor-status` si no hay movimiento) el nivel de batería, estado de GPS activo y modo offline.
  5. **Integración con Google Maps Compose:** Marcadores de familiares, cálculo visual de velocidades y badges de sensores (batería, sin señal, GPS desactivado).
  6. Redirección de coordenadas mediante Intent nativo a la app de mapas externa (Google Maps / Apple Maps).

---

### 🟢 Etapa 5: Conducción y Detección de Accidentes (Acelerómetro)
* **Objetivo:** Portar la lógica inteligente de trayectos y detección crítica de colisiones en coche.
* **Tareas:**
  1. **Histéresis de Conducción:** Implementar en el Foreground Service el filtro de velocidad (inicio tras 1 min > 25 km/h, apagado tras 2 min de detención).
  2. **Detección de Impactos:** Escuchar el acelerómetro (`Sensor.TYPE_ACCELEROMETER`) mientras la conducción esté activa. Aplicar la lógica vectorial de aceleración:
     $$\text{Acc. Neta} = \sqrt{x^2 + y^2 + z^2} \ge 4.5\text{G}$$
     seguida de 3 segundos de inmovilidad (varianza $< 0.15\text{G}$).
  3. **Pantalla Crítica de Pre-Alerta:** Overlay a pantalla completa con sirena en bucle (usando `MediaPlayer`) y cuenta regresiva de 15 segundos.
  4. Botón de "Falsa Alarma" en la app para anular el impacto y cancelar la alerta en el servidor (llamando a `/api/alerts/crash/{id}/false-alarm`).
  5. Disparo automático tras 15s sin responder: petición a la API de colisión, inicio de tracking rápido (5s) y envío de SMS local directo a contactos como canal de respaldo.

---

### 🟢 Etapa 6: Alertas Críticas (SOS) y Geocercas Dinámicas
* **Objetivo:** Portar el S.O.S. silencioso y el radar de proximidad relativa.
* **Tareas:**
  1. **SOS Silencioso:** Botón destacado en Panel. Al presionarse, inicia el tracking de alta frecuencia (5s) y arranca la grabación ambiental silenciosa de 15 segundos con `MediaRecorder`.
  2. Subida asíncrona del archivo de audio grabado a `/api/emergency-alerts/{id}/audio` mediante multipart/form-data.
  3. **Radar de Proximidad:** Notificaciones locales inmediatas y vibración persistente (`Vibrator`) al recibir alertas de geocercas móviles sobre la distancia relativa de otros miembros.

---

### 🟢 Etapa 7: Gestión de Núcleos y Stripe SDK
* **Objetivo:** Completar la administración de miembros del núcleo y el checkout de pagos premium.
* **Tareas:**
  1. **Gestión de Núcleos (Tab 3: Familia):** Unirse a núcleos mediante códigos, crear nuevos núcleos, copiar código al portapapeles y remover/salir de núcleos.
  2. **Stripe SDK Mobile:** Integrar el componente nativo de Stripe `PaymentSheet` para autenticación segura 3D Secure/SCA.
  3. Integrar un modo simulador robusto en local para pruebas de facturación en la pestaña Ajustes.
  4. Ejecución de la suite completa de pruebas unitarias y de instrumentación en Android Studio.

---

## ⚠️ Consideraciones de Batería y Permisos Críticos

Nativo Android requiere la solicitud y manejo cuidadoso de permisos sensibles en tiempo de ejecución:
1. `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` (Ubicación en primer plano).
2. `ACCESS_BACKGROUND_LOCATION` (Ubicación en segundo plano, requiere explicación detallada al usuario según políticas de Google Play).
3. `FOREGROUND_SERVICE` y `FOREGROUND_SERVICE_LOCATION` (Requerido para Android 14+).
4. `POST_NOTIFICATIONS` (Notificaciones en Android 13+).
5. `SEND_SMS` (Requerido para el fallback de SMS de emergencia local).
6. `RECORD_AUDIO` (Requerido para el SOS ambiental).
7. `BODY_SENSORS` / `ACTIVITY_RECOGNITION` (Podómetro para el check-in pasivo).

---

## 🧪 Estrategia de Pruebas y Validación de API
* **Configuración Local:** Dado que el backend corre en contenedores Docker en local, se utilizará la IP por defecto del emulador de Android para conectarse a la máquina host (`10.0.2.2:8000`) o bien la URL dinámica/estática del túnel de **ngrok**.
* **Prueba de No Regresión:** Durante todo el desarrollo, se ejecutará el comando:
  ```bash
  docker compose exec backend php artisan test
  ```
  Esto garantizará que ningún cambio o nueva petición proveniente del desarrollo nativo en Kotlin rompa las APIs del backend de Laravel.
