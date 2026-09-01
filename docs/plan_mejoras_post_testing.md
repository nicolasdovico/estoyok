# Plan de Implementación de Mejoras (Post-Testing Feedback)

Este documento estructura el plan de trabajo para implementar de forma ordenada y sin riesgos las oportunidades de mejora identificadas en el informe de **Testers Community** ([`docs/estoy_feedback.pdf`](file:///home/usuario/aplicaciones/estoyok/docs/estoy_feedback.pdf)).

---

## 🎯 Objetivos y Priorización

| Fase | Módulo / Mejora | Impacto | Complejidad | Tipo de Trabajo |
| :--- | :--- | :--- | :--- | :--- |
| **Fase 1** | **ASO y Capturas de Play Store** | 🟢 Alto (Conversión & Descargas) | 🟡 Baja | Ficha de Play Store (Marketing/Copy) |
| **Fase 2** | **Walkthrough / Onboarding Interactivo** | 🟢 Alto (Retención y Activación) | 🟡 Media | Android Nativo (Jetpack Compose) |
| **Fase 3** | **Google Sign-In (OAuth)** | 🟢 Alto (Reducción de fricción) | 🟡 Media | Android (Credential Manager) + Laravel API |
| **Fase 4** | **Internacionalización (i18n Español / Inglés)** | 🔵 Medio (Expansión Global) | 🔴 Media-Alta | Android (`strings.xml`) + Backend (`lang/`) |

---

## 📋 Detalle de Fases de Ejecución

### 🚀 FASE 1: Optimización de la Ficha de Google Play (ASO y Creativos)
* **Objetivo:** Maximizar la visibilidad en el buscador de Google Play y aumentar el ratio de instalación de visitantes a usuarios activos.
* **Tareas:**
  1. **Optimización ASO (Textos):**
     * **Título:** `Estoy Ok: Seguridad Familiar` (30 caracteres).
     * **Descripción Corta (80 car):** *Localizador GPS familiar en tiempo real, botón SOS silencioso y check-in diario.*
     * **Descripción Completa:** Redacción enriquecida con palabras clave (*rastreo satelital, geocercas, zonas seguras, detector de choques, alerta de bienestar, control parental, Life360 alternativa*).
  2. **Diseño de Screenshots para Play Store:**
     * Generar capturas de 1080x1920 con encabezados explicativos:
       * *Captura 1:* **Rastreo Familiar en Vivo** (Mapa con avatares y zonas seguras).
       * *Captura 2:* **Check-in Diario de Bienestar** (Contador y botón Estoy OK).
       * *Captura 3:* **Alerta SOS y Detección de Choques** (Asistencia de emergencia).
       * *Captura 4:* **Seguridad Vehicular** (Scoring y excesos de velocidad).

---

### 🎨 FASE 2: Onboarding / Walkthrough Dinámico e Interactivo
* **Objetivo:** Guiar al nuevo usuario en su primera apertura explicando el valor de Estoy Ok antes del registro, aumentando la retención.
* **Componentes Técnicos (Android Compose):**
  1. Crear `WalkthroughScreen.kt` con `HorizontalPager` (Accompanist / Compose Foundation).
  2. **Páginas del Carrusel:**
     * **Slide 1:** *Cuidado Pasivo y Bienestar* (Iconografía de escudo y botón Estoy OK).
     * **Slide 2:** *Rastreo en Tiempo Real y Zonas Seguras* (Mapa y alertas automáticas).
     * **Slide 3:** *Protección Crítica SOS y Vial* (Grabación silenciosa y detección de accidentes).
  3. **Controles de Navegación:** Indicador de puntos (*Page Indicator*), botón *"Omitir"* en la esquina superior y botón destacado *"Comenzar"* en el último slide.
  4. **Persistencia:** Guardar flag `HAS_SEEN_ONBOARDING` en `SessionManager` (DataStore) para mostrarlo solo en la primera apertura.

---

### 🔐 FASE 3: Autenticación con Google Sign-In (OAuth 2.0)
* **Objetivo:** Permitir el registro e inicio de sesión en 1 toque mediante la cuenta de Google vinculada al dispositivo.
* **Componentes Técnicos:**
  1. **Backend (Laravel API):**
     * Crear endpoint `POST /api/auth/google` en `AuthController.php`.
     * Validar el `id_token` de Google mediante `google/apiclient` o verificación directa de clave pública de Google.
     * Si el usuario existe, emitir token Sanctum; si es nuevo, registrarlo con `email_verified_at = now()` (sin requerir OTP) y asignarle su avatar de Google.
  2. **Android Nativo (Kotlin):**
     * Integrar Google Credential Manager (`androidx.credentials:credentials`).
     * Añadir botón estilizado *"Continuar con Google"* en `LoginScreen.kt` y `RegisterScreen.kt`.
     * Conectar el flujo en `LoginViewModel.kt` y sincronizar sesión en `SessionManager`.

---

### 🌍 FASE 4: Soporte Multi-Idioma e Internacionalización (i18n)
* **Objetivo:** Permitir que usuarios de habla inglesa y de otras regiones utilicen la plataforma en su idioma nativo.
* **Componentes Técnicos:**
  1. **Android Nativo:**
     * Extraer textos hardcodeados de Compose a `res/values/strings.xml` (Español) y `res/values-en/strings.xml` (Inglés).
     * Añadir selector de idioma en `AjustesScreen.kt` con soporte de `LocaleManager` (Android 13+ App-Specific Language).
  2. **Backend (Laravel):**
     * Configurar diccionarios `lang/en/` para validaciones, correos transaccionales y notificaciones push.
     * Respetar la cabecera HTTP `Accept-Language: en` enviada por la app móvil.

---

## 📅 Cronograma Sugerido

* **Semana 1 (Durante el cierre de los 14 días):** Ejecutar **Fase 1** (ASO y Creativos en Google Play Console).
* **Semana 2 (Al solicitar/obtener Producción):** Implementar **Fase 2** (Onboarding Walkthrough).
* **Semana 3:** Implementar **Fase 3** (Google Sign-in).
* **Semana 4:** Implementar **Fase 4** (i18n Inglés/Español).
