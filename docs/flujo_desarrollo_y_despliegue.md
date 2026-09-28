# Guía de Flujo de Trabajo: Desarrollo (Dev) vs Producción (Prod)

Este documento detalla el procedimiento operativo estándar para desarrollar, probar en la calle con datos móviles (4G) y promover cambios a producción en **Estoy Ok**.

---

## 🗺️ Mapa de Arquitectura de Entornos

| Componente | 🧪 Entorno de Desarrollo (Dev) | 🚀 Entorno de Producción (Prod) |
| :--- | :--- | :--- |
| **Rama de GitHub** | `dev` | `main` |
| **Backend en Railway** | `https://backend-api-dev-2a56.up.railway.app/api/` | `https://api.estoyok24.com/api/` |
| **Base de Datos** | PostgreSQL Dev (Datos de prueba / Laboratorio) | PostgreSQL Prod (**Sagrada - Usuarios reales**) |
| **Trigger de Deploy** | `git push origin dev` | `git push origin main` |
| **Tipo de Build Android** | `debug` | `release` |
| **Destino de la App** | Tu teléfono físico (instalación local USB / APK) | **Google Play Store** (Bundle `.aab`) |

---

## 🔄 Flujo 1: Crear una Feature y Probarla en Dev (Pruebas en la Calle)

Usa este flujo cada vez que vayas a programar una mejora, corrección o nueva funcionalidad que requiera salir a la calle a probar con el auto o datos móviles.

### Paso 1: Asegurarte de estar en la rama `dev`
```bash
git checkout dev
git pull origin dev
```

### Paso 2: Desarrollar y verificar localmente
* Modifica los archivos en el backend o en la app móvil.
* Si tocaste código del backend, corre la suite de pruebas unitarias para no romper nada:
```bash
./test.sh
# o dentro de docker:
docker compose exec backend php artisan test
```

### Paso 3: Subir el backend a Railway Dev
Guarda tus cambios y súbelos a la rama `dev`:
```bash
git add .
git commit -m "feat(modulo): descripcion de la mejora realizada"
git push origin dev
```
> [!NOTE]
> Railway detectará el push en `dev`, compilará el backend, ejecutará las migraciones pendientes automáticamente (`php artisan migrate --force`) y levantará el servicio en `backend-api-dev-2a56.up.railway.app`.

---

### Paso 4: Compilar e instalar la app de prueba en tu celular (Gradle)

Conecta tu celular por cable USB con la *Depuración por USB* activada.

#### Opción A (La más rápida - Compilar e Instalar directo):
Desde la carpeta raíz del proyecto o desde `android-native/`:
```bash
cd android-native
./gradlew installDebug
```
*Esto compila la app en modo `debug`, la inyecta automáticamente en tu teléfono y la deja lista para abrir.*

#### Opción B (Generar el archivo APK para pasártelo):
Si prefieres generar el instalador `.apk`:
```bash
cd android-native
./gradlew assembleDebug
```
* **Ubicación del APK generado:**
  `android-native/app/build/outputs/apk/debug/app-debug.apk`

---

### Paso 5: Prueba de Campo en Condiciones Reales (4G)
1. Desconecta el teléfono de la computadora.
2. Abre la app en tu celular.
3. Sal a la calle en el vehículo o a pie con datos móviles (4G):
   * La app se comunicará directamente con `backend-api-dev-2a56.up.railway.app`.
   * Registra ubicaciones, valida exceso de velocidad, frenadas bruscas, geocercas y SOS.
   * **Seguridad total:** Puedes borrar usuarios, resetear contraseñas o disparar alertas en Dev con la tranquilidad de que ningún usuario de Google Play será afectado.

---

## 🚀 Flujo 2: Promoción a Producción (Google Play Store)

Una vez que la funcionalidad fue probada y validada con éxito en la calle, es momento de lanzarla oficialmente.

### Paso 1: Incrementar la versión en Android
Abre [`android-native/app/build.gradle.kts`](file:///home/usuario/aplicaciones/estoyok/android-native/app/build.gradle.kts) e incrementa el número de compilación y versión:
```kotlin
defaultConfig {
    // ...
    versionCode = 14       // Incrementar siempre en +1 para Google Play
    versionName = "1.0.7"    // Nueva versión semántica
}
```
*(Opcional: actualiza el texto de versión en `AjustesScreen.kt` para coincidir con `v1.0.7 (Compilación 14)`).*

### Paso 2: Fusionar `dev` hacia `main` y desplegar el backend
```bash
# 1. Volver a main
git checkout main
git pull origin main

# 2. Fusionar los cambios probados de dev
git merge dev

# 3. Subir a producción
git push origin main
```
> [!IMPORTANT]
> Al hacer `git push origin main`, Railway actualizará inmediatamente el backend oficial de producción (`https://api.estoyok24.com/api/`).

---

### Paso 3: Compilar el Bundle de Producción (.aab) para Google Play

Ejecuta el comando Gradle para empaquetar y firmar la versión de lanzamiento:
```bash
cd android-native
./gradlew bundleRelease
```

* **Ubicación del paquete generado:**
  `android-native/app/build/outputs/bundle/release/app-release.aab`

---

### Paso 4: Subir a Google Play Console
1. Entra a [Google Play Console](https://play.google.com/console).
2. Selecciona la aplicación **Estoy Ok**.
3. **Recomendación estándar de la industria:**
   * Ve a **Pruebas internas** (*Internal Testing*) $\rightarrow$ **Crear nueva versión**.
   * Sube el archivo `app-release.aab`.
   * En 15 minutos se habilita para que tú lo descargues desde la Play Store oficial en tu teléfono y verifiques que la versión firmada corre perfecta.
4. **Pase a Producción:**
   * En la misma versión de Pruebas Internas, haz clic en **Promocionar versión $\rightarrow$ Producción**.
   * Envía los cambios a revisión de Google.

---

## 🧰 Cheat Sheet: Comandos de Gradle Más Utilizados

Todos estos comandos se ejecutan situándote en la carpeta `android-native/`:

| Tarea | Comando |
| :--- | :--- |
| **Instalar en celular (Dev)** | `./gradlew installDebug` |
| **Generar APK de prueba** | `./gradlew assembleDebug` |
| **Generar Bundle de Google Play** | `./gradlew bundleRelease` |
| **Limpiar caché de compilación** | `./gradlew clean` |
| **Limpiar y recompilar Release** | `./gradlew clean bundleRelease` |
| **Ver errores detallados de build** | `./gradlew bundleRelease --stacktrace` |
| **Ver lista de tareas Gradle** | `./gradlew tasks` |

---

## 🛡️ Reglas de Oro de Seguridad

1. **Retrocompatibilidad de API:** Los usuarios de Google Play tardan días o semanas en actualizar la app. Cuando agregues nuevos endpoints o columnas en la base de datos de producción, asegúrate de que sean aditivos y no rompan la versión que los usuarios ya tienen instalada.
2. **Aislamiento de Tests:** Nunca ejecutes `./test.sh` configurado contra la base de datos de producción. Los tests siempre corren en Docker local o SQLite en memoria (`RefreshDatabase`).
3. **Archivo `local.properties`:** Nunca quites `local.properties` del `.gitignore`. Ahí reside tu `DEV_API_URL` local y las contraseñas de firmado de tu certificado.
