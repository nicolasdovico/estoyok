# Plan de Depreciación del Frontend Web Funcional e Informatización Comercial

Este documento detalla el plan para reestructurar el frontend web de **Estoy Ok** para que actúe exclusivamente como una landing page comercial/informativa. Toda la funcionalidad operativa y el seguimiento de usuario final se trasladan de forma exclusiva a las aplicaciones móviles (Android en `android-native/` e iOS en `mobile/`).

---

## 🎯 Objetivos del Plan

1. **Enfoque Móvil Exclusivo:** La gestión de núcleos, check-ins, historial de bienestar, configuración de seguridad, notificaciones, cobros y radar de proximidad se realizarán únicamente desde las apps móviles.
2. **Reversibilidad y Preservación del Código (No Destructivo):** No se eliminará ningún archivo de lógica de la web (como `Dashboard.tsx`, `BillingSection.tsx`, etc.). Se utilizarán cortocircuitos en las rutas (`/login`, `/register`, `/verify-email`) y en el componente principal (`page.tsx`) para desactivar el acceso visual de forma 100% reversible.
3. **Mantenimiento de la Pantalla de Crisis:** La ruta pública de emergencia `/emergencia/[id]` debe seguir estando completamente disponible y operativa. Es el canal crítico mediante el cual los contactos de emergencia visualizan el mapa en vivo y envían feedback de apoyo ("Voy en camino", etc.) sin necesidad de estar logueados o tener la app móvil instalada.
4. **Actualización de Enlaces en Canales de Alerta:** Asegurarse de que el flujo de enlace público en los SMS/WhatsApp de emergencia se mantenga operativo.

---

## 🛠️ Detalle de Tareas de Implementación

### 1. Cortocircuito de Autenticación y Dashboard en la Home (`frontend-web/src/app/page.tsx`)
* **Lógica de Autenticación:** Comentar o eliminar la evaluación del `auth_token` de `localStorage` que renderiza el componente `<Dashboard />` para que los usuarios logueados previamente en la web ya no vean el panel operativo.
* **Header / Barra de Navegación:**
  * Ocultar los enlaces de "Iniciar Sesión" y "Registrarse".
  * Reemplazarlos por un botón destacado que diga "Descargar App" (que desplace al usuario a la nueva sección de descarga).
* **Sección Hero (Llamadas a la Acción - CTAs):**
  * Cambiar el botón "Registrarse Gratis" por dos botones/badges promocionales: "Descargar para Android" y "Descargar para iOS".
* **Mockups Visuales:**
  * Actualizar el microcopy de los simuladores en la landing para enfatizar que corresponden a la experiencia de la aplicación móvil.

### 2. Desactivación Reversible de Páginas de Autenticación
Para las siguientes rutas clave, se aplicará un "Early Return" en el componente que muestre un banner informativo con enlaces de descarga de las apps, dejando el código del formulario intacto debajo:
* **Ruta `/login` (`src/app/login/page.tsx`)**
* **Ruta `/register` (`src/app/register/page.tsx`)**
* **Ruta `/verify-email` (`src/app/verify-email/page.tsx`)**

*Diseño de la Pantalla Informativa en estas rutas:*
* Estilo premium oscuro acorde a la landing page.
* Mensaje claro: *"La funcionalidad de usuario (Ingreso, Registro y Configuración) ahora está disponible exclusivamente en las aplicaciones móviles. Descarga Estoy Ok para tu smartphone y continúa protegiendo a los tuyos."*
* Botones directos para:
  * Descargar APK / Google Play.
  * Descargar iOS App.
  * Botón de retorno al inicio ("Volver a la Web").

### 3. Nueva Sección de Descarga en la Landing Page
* Diseñar una sección premium interactiva (`#download`) en `src/app/page.tsx` con:
  * Códigos QR simulados para descargas directas.
  * Botones de tiendas (App Store / Google Play).
  * Enlaces para descargar directamente el archivo APK (para pruebas en Android).

### 4. Preservación y Validación
* **Crisis Screen (`src/app/emergencia/[id]`)**: Verificar que la pantalla siga cargando el mapa de Leaflet, el polling de 5 segundos para SOS/Accidente, el reproductor de audio ambiente, y las respuestas interactivas del contacto.
* **Ejecución de Linter y Compilación:** Asegurar que los cambios no rompan TypeScript ni la compilación de producción de Next.js (`npm run build`).

---

## 📅 Impacto en Futuras Sesiones de Desarrollo

* **Desarrollo de Clientes:** Toda nueva característica visual o lógica de usuario final (ej. configuración de alertas, reordenar contactos, auto-checkins) deberá realizarse exclusivamente en:
  * Kotlin Nativo en `/android-native`
  * Expo / React Native en `/mobile`
* **Backend y API:** Los endpoints de la API de Laravel (`/api/...`) y el backoffice Filament siguen funcionando exactamente igual para dar servicio a las apps móviles.
* **Restauración:** Si el proyecto decide re-habilitar el flujo web en el futuro, solo se requerirá retirar los cortocircuitos (early returns) aplicados y restablecer los enlaces en `page.tsx`.

---

## 📝 Actualización de Documentación
Una vez aprobado el plan, se realizarán las siguientes modificaciones en los documentos del proyecto:
1. **Actualización de `docs/status.md`:** Se agregará la **Fase 13: Depreciación Web y Redirección Móvil** marcando el nuevo enfoque comercial del sitio web.
2. **Registro en `docs/progress.txt`:** Se documentarán los cambios aplicados en la estructura del frontend web.
