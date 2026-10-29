# Plan de Activación Temprana, Onboarding Interactivo y Release v17 (1.1.0)

**Fecha:** 28 de Octubre de 2026  
**Objetivo:** Eliminar la barrera de adopción y abandono inicial ("The Empty State Paradox"), guiando a los nuevos usuarios registrados para que descubran y experimenten de inmediato el valor central de la app (Bienestar diario, Zonas Seguras, Contactos de Confianza y SOS), agrupando además las mejoras y correcciones pendientes en una única versión consolidada **v17 (1.1.0)**.

---

## 1. Diagnóstico del Comportamiento del Usuario

Los usuarios adquiridos mediante campañas (Meta Ads / Instagram Reels) u orgánicos se registran y se encuentran ante una interfaz sin una misión clara:
1. No saben qué hace el botón central "Estoy OK".
2. No comprenden que el sistema envía un WhatsApp automático si no confirman su bienestar.
3. Desconocen la función de Zonas Seguras (geocercas de llegada/salida sin mensajes).
4. No agregan contactos de emergencia inmediatamente, dejando la app inoperativa en su capa pasiva y activa.

---

## 2. Estrategia 360° Inspirada en los Grandes del Sector (Life360, Duolingo, Uber)

### Eje A: Dentro de la App (Product-Led Onboarding & Gamificación)
1. **Checklist de Primeros Pasos ("Tu Escudo de Seguridad"):**
   - Tarjeta visible y elegante en la parte superior del panel principal (debajo de la barra de usuario).
   - Barra de progreso interactiva (0% a 100%) con 3 misiones simples:
     - [ ] **Misión 1: "Confirmá tu bienestar hoy"** (Tocar el botón central "Estoy OK"). Al tocarlo, animación verde y tilde inmediato.
     - [ ] **Misión 2: "Agregá a quién te cuidará"** (Agregar 1 contacto de confianza con acceso directo al modal o WhatsApp de prueba).
     - [ ] **Misión 3: "Creá tu primera Zona Segura"** (Ej: Casa o Trabajo con pulsación en el mapa o autocompletado).
   - Estado completado: Tarjeta se convierte en un banner de éxito verde esmeralda: *"¡Tu protección está 100% activa! 🛡️"* con opción de minimizar o descartar.
2. **Tooltips Contextuales / Spotlight:**
   - Burbujas de foco guiado la primera vez que se accede a cada pantalla explicando en 1 sola frase para qué sirve cada elemento.
3. **Empty States Educativos y Accionables:**
   - En Zonas Seguras vacías: Ilustración y botón prominente *"¿Tu hijo o familiar llegó bien? + Crear zona Casa/Colegio"*.
   - En Contactos vacíos: *"Si algún día no confirmás tu bienestar, ¿a quién le avisamos por WhatsApp? + Agregar contacto"*.

### Eje B: En Instagram (@estoyok24)
1. **Estrategia "1 Reel = 1 Caso de Uso Real":**
   - Reel A: *"Mamá vive sola"* (Check-in matutino vs llamadas invasivas).
   - Reel B: *"Avisame cuando llegues"* (Zonas seguras de entrada y salida automáticas).
   - Reel C: *"Volver segura de noche / Taxi"* (SOS silencioso con 4 toques en el botón de encendido).
2. **Historias Destacadas (Manual Visual Permanente):**
   - 🟢 *¿Cómo funciona?* (Paso a paso del botón verde).
   - 📍 *Zonas Seguras* (Video de 15s creando un perímetro).
   - 🚨 *SOS Oculto* (Demostración de los 4 toques en el bolsillo).
   - ❓ *Preguntas Frecuentes* (Batería, privacidad, costo).

### Eje C: En la Web (`estoyok24.com`)
1. **Titulares por Rol de Usuario:** Segmentar la propuesta para "Personas que viven solas", "Padres de familia" y "Conductores / Emergencias".
2. **Micro-demostraciones animadas (GIFs/WebM):** Mostrar en 3 segundos el toque del botón y el mensaje de WhatsApp llegando al familiar.

---

## 3. Alcance Integral de la Versión v17 (1.1.0)

Para maximizar el impacto y publicar una actualización sólida, agruparemos el checklist de activación con las tareas técnicas ya agendadas en el backlog:

### Bloque 1: Activación Temprana y UX (Nuevo)
- [ ] **Checklist Interactivo de Primeros Pasos en Android (`SecurityChecklistCard.kt`):**
  - Componente Jetpack Compose en `PanelScreen.kt` o `MapaScreen.kt`.
  - Conectado reactivamente con el estado del usuario (`has_checked_in_today`, `contacts_count > 0`, `geofences_count > 0`).
  - Animación de tildado y progreso fluido.
  - Persistencia de descarte si el usuario prefiere no verla tras completar los pasos.
- [ ] **Empty States accionables:**
  - Rediseño amigable y educativo de listas vacías en contactos y perímetros.

### Bloque 2: Actualizaciones In-App de Google Play (Backlog v17)
- [ ] **Google Play In-App Updates:**
  - Integrar la biblioteca oficial `com.google.android.play:app-update` (o `app-update-ktx`).
  - Detección en `MainActivity.kt` al iniciar la app (`AppUpdateManager.getAppUpdateInfo()`).
  - Modo *Flexible* para actualizaciones normales (descarga en segundo plano y snackbar para reiniciar) y modo *Inmediato* para parches críticos.
  - Resuelve la lentitud de la tienda de Google Play que retiene el botón "Abrir" por caché.

### Bloque 3: Refuerzo de Google Sign-In & Credential Manager (Backlog v17)
- [ ] **Compatibilidad ampliada con `GetSignInWithGoogleOption`:**
  - En `LoginViewModel.kt` y `RegisterViewModel.kt`, integrar `GetSignInWithGoogleOption` junto con `GetGoogleIdOption`.
  - Asegurar compatibilidad en dispositivos con versiones de Google Play Services anteriores o tras cancelaciones.
  - Sincronizar el archivo local `google-services.json` con la huella SHA-1 de producción de Google Play Store (`65:cd:23:e5:f6:10:9e:9c:7a:df:78:ae:e5:5b:cf:3d:82:8b:7d:34`).

### Bloque 4: Calidad y Despliegue
- [ ] Ejecución de la suite `./test.sh` y tests de backend.
- [ ] Actualización de `versionCode = 17` y `versionName = "1.1.0"`.
- [ ] Generación y firma del bundle de producción `app-release.aab`.
- [ ] Despliegue en Railway (`dev` y `main`).
