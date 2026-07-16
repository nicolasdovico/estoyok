# Notas de Trabajo: Pendientes de Avatares en el Mapa (Estoy Ok)

Este documento registra el estado actual del desarrollo de avatares para continuar el trabajo en la próxima sesión.

---

## 1. Estado Actual de la Funcionalidad

* **Lo que funciona (Éxito):**
    * La subida de avatares al backend en Railway persiste correctamente.
    * El proxy reverso de Railway se configuró correctamente para usar HTTPS dinámico (`X-Forwarded-Proto`).
    * El enlace simbólico en Railway se corrigió (se ejecuta en `startCommand` de forma relativa `--relative` con soporte de `symfony/filesystem`).
    * Las imágenes de los avatares **se visualizan correctamente** en:
        * La solapa de **Vehículos** (selector horizontal de familiares).
        * La solapa de **Recorridos** (historial de rutas).
    * El sistema de fallback e iniciales locales ante fallos de red o errores 404 funciona de forma fluida.

* **Lo que NO funciona (Pendiente):**
    * Los marcadores sobre el mapa (`MarkerComposable` en `MapaScreen.kt`) **siguen mostrando las iniciales** en el teléfono físico conectado a Railway, a pesar de que la descarga de la imagen por red es exitosa en las otras pantallas.

---

## 2. Cambios de Código Realizados en la Sesión

* **Backend (`User.php`):**
    * Modificado `getAvatarUrlAttribute` para resolver la URL del host de forma dinámica y forzar HTTPS utilizando la cabecera `X-Forwarded-Proto`.
    * Añadida dependencia `symfony/filesystem` vía Composer.
* **Despliegue (`railway.json`):**
    * Se configuró la creación de enlaces simbólicos relativos (`rm -rf public/storage && php artisan storage:link --relative`) en la sección `startCommand` para persistir en el contenedor activo.
* **Frontend Mobile (`MapaScreen.kt`):**
    * Implementada la pre-descarga concurrente de imágenes a objetos `Bitmap` mediante `snapshotFlow` y `collect` en un `LaunchedEffect(Unit)` a nivel de pantalla.
    * Los bitmaps descargados se almacenan en un mapa observable `markerBitmaps` indexado por el ID del miembro (`Int`).
    * En el marcador del mapa, se dibuja `Image(bitmap = bitmapToDraw.asImageBitmap())` si existe en el mapa, o se cae al fallback de iniciales si es nulo.

---

## 3. Hipótesis y Líneas de Investigación para Mañana

Dado que la descarga de la imagen por red **funciona perfectamente** en otras partes de la app con la misma URL, el fallo es estrictamente de renderizado/actualización del marcador del mapa en Compose. Mañana se deben verificar las siguientes hipótesis:

1. **Bug de Invalidation de `MarkerComposable`:**
   * Aunque `markerBitmaps` se actualice con el bitmap descargado y el código dentro de `MarkerComposable` se recomponga, la librería de Google Maps podría no estar enterándose del cambio de estado porque el bitmap se asigna de forma asíncrona.
   * **Posible solución:** En lugar de renderizar una vista Compose dinámica compleja dentro de `MarkerComposable` (que se convierte a Bitmap descriptor de forma inestable), investigar el uso de la propiedad `icon` de un componente `Marker` estándar, construyendo el `BitmapDescriptor` dinámicamente con un Canvas de Android a partir del bitmap descargado.

2. **Falla en el ImageRequest de Coil dentro del LaunchedEffect:**
   * Es posible que `context.imageLoader.execute(request)` no esté utilizando el mismo pipeline de caché que `AsyncImage` o esté fallando silenciosamente por falta de algún parámetro (ej. headers de autenticación, aunque el endpoint es público).
   * **Acción:** Añadir logs detallados en el bloque `catch` y evaluar si el `result` es de tipo `ErrorResult` y qué excepción arroja.

3. **Problema con Hardware Bitmaps:**
   * Aunque se configuró `.allowHardware(false)`, si el bitmap resultante sigue considerándose de hardware en ciertos dispositivos físicos, `asImageBitmap()` o el renderizador de Google Maps podrían estar descartándolo silenciosamente para evitar crasheos.
