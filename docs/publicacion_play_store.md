# Guía Paso a Paso: Publicación de "Estoy Ok" en Google Play Store

Este documento contiene la guía completa y detallada para compilar, configurar y publicar la aplicación nativa **Estoy Ok** (Android Kotlin) en la **Google Play Store**, incluyendo el cumplimiento obligatorio de la normativa de pruebas para cuentas personales.

---

## 1. Requisitos Previos

1. **Cuenta de Desarrollador en Google Play Console**:
   - Registrarse en [play.google.com/console](https://play.google.com/console).
   - Abonar la cuota única de registro de **$25 USD**.
   - Completar la verificación de identidad del titular (DNI / Pasaporte).

2. **Política de Privacidad Pública (Obligatoria)**:
   - Google exige publicar una URL accesible vía HTTPS con la política de privacidad.
   - Se utilizará la ruta del sitio web: `https://frontend-web-production-f4f0.up.railway.app/politica-de-privacidad` (o el dominio final `https://estoyok.app/privacidad`).

---

## 2. Requisito Obligatorio: 20 Evaluadores durante 14 Días (Cuentas Personales)

Google exige que todas las **cuentas personales registradas a partir del 13 de noviembre de 2023** completen una fase obligatoria de pruebas cerradas antes de poder publicar abiertamente.

### Flujo del Proceso:
```
[1. Compilar .aab] ➔ [2. Subir a Pruebas Cerradas] ➔ [3. 20 Testers x 14 Días] ➔ [4. Cuestionario] ➔ [5. Publicación Abierta]
```

### Paso a Paso de las Pruebas Cerradas (*Closed Testing*):
1. **Configurar la Vía de Pruebas Cerradas**:
   - En Play Console, ir a **Pruebas** -> **Pruebas cerradas** (*Closed Testing*).
   - Crear una nueva lista de probadores e ingresar al menos **25 a 30 direcciones de correo Gmail** (amigos, familiares o contactos de prueba). Se recomienda superar los 20 mínimos para prevenir demoras si alguien desinstala la app.
2. **Enviar Invitaciones y Enlace de Descarga**:
   - Google generará un enlace privado de invitación (ej. `https://play.google.com/apps/testing/com.estoyok.app`).
   - Cada probador debe ingresar al enlace, aceptar unirse al programa beta e instalar la app desde Google Play en su teléfono.
3. **Periodo de 14 Días Consecutivos**:
   - La app debe mantenerse instalada y activa en al menos 20 dispositivos durante **14 días seguidos**.
   - Durante este periodo puedes subir nuevas versiones corregidas (`.aab`) sin reiniciar el contador de 14 días.
4. **Solicitar Acceso a Producción**:
   - Cumplido el día 14, la consola activará el botón **"Solicitar acceso a Producción"**.
   - Completar el formulario de 3 preguntas de Google sobre los comentarios recibidos de los evaluadores y las mejoras aplicadas.

---

## 3. Compilación del Android App Bundle (`.aab`)

Google Play exige el formato **Android App Bundle (`.aab`)** para producción.

### Paso 1: Generar la Clave de Firma Digital (*Keystore*)
Ejecutar en la terminal para crear la firma de producción:
```bash
keytool -genkey -v -keystore estoyok-release.jks -alias estoyok-key -keyalg RSA -keysize 2048 -validity 10000
```
> [!IMPORTANT]
> Guarda el archivo `estoyok-release.jks` y sus contraseñas en un lugar seguro. Sin esta clave no se podrán publicar actualizaciones futuras.

### Paso 2: Configurar la Firma en el Proyecto
1. Copiar `estoyok-release.jks` a la carpeta `/android-native/app/`.
2. Editar [android-native/app/build.gradle.kts](file:///home/usuario/aplicaciones/estoyok/android-native/app/build.gradle.kts) agregando las credenciales:
   ```kotlin
   android {
       ...
       signingConfigs {
           create("release") {
               storeFile = file("estoyok-release.jks")
               storePassword = "TU_CONTRASEÑA"
               keyAlias = "estoyok-key"
               keyPassword = "TU_CONTRASEÑA"
           }
       }
       buildTypes {
           release {
               isMinifyEnabled = true
               proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

### Paso 3: Compilar el Bundle de Producción
Ejecutar en la consola:
```bash
cd android-native
./gradlew bundleRelease
```
El archivo resultante se generará en:  
`android-native/app/build/outputs/bundle/release/app-release.aab`

---

## 4. Material Gráfico y Ficha de la Tienda (*Store Listing*)

En **Google Play Console** -> **Ficha de la tienda principal**:

| Elemento | Requisito de Formato | Dimensiones | Descripción |
| :--- | :--- | :--- | :--- |
| **Icono de la App** | PNG (sin transparencias en el borde) | 512 x 512 px | Usar el isotipo centrado sobre fondo blanco ([docs/logo-square.png](file:///home/usuario/aplicaciones/estoyok/docs/logo-square.png)). |
| **Gráfico de Funciones** | PNG o JPEG | 1024 x 500 px | Banner promocional de cabecera con el logotipo y eslogan. |
| **Capturas de Pantalla** | PNG o JPEG (mínimo 4) | 16:9 o 9:16 (ej. 1080 x 1920 px) | Screenshots mostrando: Mapa interactivo, Panel de Bienestar, Wi-Fi Seguro y Alertas SOS. |

### Textos de Comercialización:
- **Nombre de la app**: `Estoy Ok - Seguridad Familiar` (Máx. 30 caracteres).
- **Descripción corta**: `Plataforma de asistencia familiar activa y pasiva con check-in y geocercas.` (Máx. 80 caracteres).
- **Descripción completa**: Detallar las funciones de bienestar pasivo (auto-checkin por Wi-Fi), geocercas activas, botón de emergencia SOS y panel de familiares.

---

## 5. Declaraciones de Permisos Sensibles y Datos

En la sección **Contenido de la aplicación** en Play Console:

1. **Ubicación en Segundo Plano (`ACCESS_BACKGROUND_LOCATION`)**:
   - Completar la declaración justificando que la app requiere ubicación persistente para alertas de geocercas automáticas y confirmación de bienestar por Wi-Fi.
   - Grabar y adjuntar un enlace de YouTube (video no listado) de 30-60 segundos donde se observe el funcionamiento del servicio de monitoreo en segundo plano.
2. **Seguridad de los Datos (*Data Safety*)**:
   - Declarar recolección de: *Ubicación exacta y aproximada*, *Información personal (email, nombre)* y *Audio* (si aplica para SOS).
   - Confirmar que todos los datos viajan encriptados por HTTPS.
3. **Clasificación de Contenido IARC**:
   - Responder el cuestionario de clasificación de edad (Apta para todo público o mayores de 13 años).

---

## 6. Mantenimiento y Actualizaciones Futuras

Una vez superada la revisión de Producción, para publicar futuras versiones solo se requiere:
1. Incrementar `versionCode` (ej. de `1` a `2`) y `versionName` (ej. de `"1.0.0"` a `"1.0.1"`) en `build.gradle.kts`.
2. Re-compilar `./gradlew bundleRelease`.
3. Subir el nuevo `.aab` a la consola. Las actualizaciones de versiones ya aprobadas demoran habitualmente de **2 a 12 horas** en desplegarse a los usuarios.
