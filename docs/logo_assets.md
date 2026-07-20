# Guía de Assets y Tamaños de Logotipo - Estoy Ok

Este documento sirve como referencia para la preparación, exportación e integración del logotipo e iconos de la plataforma **Estoy Ok** en sus diferentes entornos (Web, Mobile Expo, Android Nativo y Panel de Administración).

---

## 1. Frontend Web (Next.js)

Los archivos de imagen para la web y la aplicación web progresiva (PWA) se configuran en el proyecto web:

| Elemento | Nombre de Archivo | Formato | Tamaño Sugerido | Ubicación en el Proyecto | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Favicon** | `favicon.ico` | `.ico` o `.png` | $32 \times 32\text{ px}$ o $48 \times 48\text{ px}$ | [favicon.ico](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/app/favicon.ico) | Icono visible en la pestaña del navegador web. |
| **Logotipo Horizontal** | `logo.png` / `logo.svg` | PNG / SVG | $250 \times 80\text{ px}$ | `/frontend-web/public/` | Logotipo con marca y nombre de la app para Navbar y cabeceras. |
| **Logotipo Cuadrado** | `logo-square.png` | PNG | $512 \times 512\text{ px}$ | `/frontend-web/public/` | Isotipo (símbolo) solo para uso general de marca. |
| **Icono PWA** | `icon.png` | PNG | $512 \times 512\text{ px}$ | `/frontend-web/public/` | Icono utilizado por el manifest para la web instalable. |
| **Apple Touch Icon** | `apple-icon.png` | PNG | $180 \times 180\text{ px}$ | `/frontend-web/public/` | Icono utilizado cuando se añade la web a inicio en iOS/Safari (sin transparencias). |

---

## 2. Frontend Mobile (Expo / React Native)

Para la aplicación multiplataforma de pruebas configurada en `mobile/app.json`:

| Elemento | Nombre de Archivo | Formato | Tamaño Sugerido | Ubicación en el Proyecto | Descripción / Requisitos |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Icono General App** | `icon.png` | PNG | $1024 \times 1024\text{ px}$ | `/mobile/assets/images/icon.png` | Cuadrado, opaco, sin transparencias. iOS le aplica los bordes curvos de forma automática. |
| **Foreground Adaptativo** | `android-icon-foreground.png` | PNG | $1024 \times 1024\text{ px}$ | `/mobile/assets/images/android-icon-foreground.png` | Isotipo sobre fondo 100% transparente. Debe ocupar el centro seguro (60-70% del área). |
| **Background Adaptativo** | `android-icon-background.png` | PNG | $1024 \times 1024\text{ px}$ | `/mobile/assets/images/android-icon-background.png` | Imagen de fondo adaptativa (opcional, por defecto usa color plano `#dc2626`). |
| **Favicon Web Expo** | `favicon.png` | PNG | $48 \times 48\text{ px}$ | `/mobile/assets/images/favicon.png` | Icono para la versión web de la app móvil. |
| **Pantalla de Inicio** | `splash-icon.png` | PNG | $2000 \times 2000\text{ px}$ | `/mobile/assets/images/splash-icon.png` | Isotipo centrado con fondo transparente. Se dibuja sobre el color de splash configurado. |

---

## 3. Android Native App (Kotlin)

El proyecto nativo de Android utiliza los recursos mipmap tradicionales y el sistema adaptativo moderno:

### Iconos de Launcher Tradicionales (Legacy)
Se ubican en sus respectivas carpetas de resolución dentro de `/android-native/app/src/main/res/`:

*   **`mipmap-mdpi/ic_launcher.png` y `ic_launcher_round.png`**: $48 \times 48\text{ px}$
*   **`mipmap-hdpi/ic_launcher.png` y `ic_launcher_round.png`**: $72 \times 72\text{ px}$
*   **`mipmap-xhdpi/ic_launcher.png` y `ic_launcher_round.png`**: $96 \times 96\text{ px}$
*   **`mipmap-xxhdpi/ic_launcher.png` y `ic_launcher_round.png`**: $144 \times 144\text{ px}$
*   **`mipmap-xxxhdpi/ic_launcher.png` y `ic_launcher_round.png`**: $192 \times 192\text{ px}$

### Icono Adaptativo Moderno (Android 8.0+)
Para una integración correcta y moderna en Android Nativo:
1.  **Frente (Foreground):** Guarda el logotipo en PNG transparente o vector XML en `/android-native/app/src/main/res/drawable/ic_launcher_foreground.png` (o `.xml`). Tamaño: $512 \times 512\text{ px}$.
2.  **Fondo (Background):** Define el color en `/android-native/app/src/main/res/values/colors.xml` o crea `/android-native/app/src/main/res/drawable/ic_launcher_background.xml` para el patrón de fondo.
3.  **Archivo de Definición:** Crea el XML de definición en `/android-native/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` (y `ic_launcher_round.xml`) apuntando al frente y fondo.
4.  **Manifiesto:** Asegúrate de que el archivo [AndroidManifest.xml](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/AndroidManifest.xml) apunte a `@mipmap/ic_launcher` y `@mipmap/ic_launcher_round` en lugar del valor del sistema por defecto.

---

## 4. Backend Admin Panel (Laravel Filament)

Para personalizar la marca en la cabecera del panel de administración:

| Elemento | Nombre de Archivo | Formato | Tamaño Sugerido | Ubicación en el Proyecto | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Logo Dashboard** | `logo.png` | PNG | $150 \times 45\text{ px}$ | `/backend/public/images/logo.png` | Formato horizontal (isotipo + texto) con transparencia. |
