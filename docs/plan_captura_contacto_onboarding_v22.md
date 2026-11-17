# Plan de Captura Temprana de Contacto y Cierre del Embudo de Suscripción (Versión 1.1.4 / Build 22)

**Fecha:** 17 de Noviembre de 2026  
**Objetivo:** Eliminar el punto ciego del embudo actual forzando o guiando la carga del primer contacto de emergencia en los primeros 60 segundos de uso, para que el 100% de los usuarios nuevos pasen por el Paywall de prueba gratis de 7 días.

---

## 🎯 1. Diagnóstico del Punto Ciego Actual

En la versión actual (1.1.3 / Build 21):
1. **El Onboarding es puramente informativo:** El usuario pasa las pantallas ilustrativas, concede permisos y cae directamente en el Mapa o en el Panel sin que nadie le pida un contacto.
2. **Dependencia de la exploración voluntaria:** Muchos usuarios tocan el botón "Estoy OK" por curiosidad, ven que se pone verde y cierran la app pensando que ya están protegidos, sin advertir que al no tener contacto cargado la alerta no le llega a nadie.
3. **El Paywall queda inactivo:** Como el Paywall de 7 días gratis está condicionado a guardarse un contacto, si el usuario no carga a nadie, el Paywall contextual nunca se muestra.

---

## 🚀 2. Las 2 Mejoras Concretas para la Próxima Versión (v1.1.4 / Build 22)

### Opción 1: Paso Guiado en el Onboarding (Inspirado en Life360 y Uber)
* **Ubicación:** Inmediatamente después de otorgar los permisos y antes de mostrar la pantalla principal.
* **Diseño y Microcopy:**
  * **Título:** *"¿A quién debemos avisar ante una emergencia?"*
  * **Subtítulo:** *"Agregá a tu familiar o persona de confianza para que reciba tu alerta y ubicación por WhatsApp."*
  * **Campos simples:**
    * Nombre: [ Ej. Mamá / Papá / Pareja ]
    * WhatsApp: [ Selector país + Teléfono móvil ]
    * Parentesco: [ Madre, Padre, Pareja, Hijo/a, Amigo/a ]
  * **Botón Principal (CTA):** `[ Guardar contacto y activar mi escudo 🛡️ ]`
  * **Botón Secundario sutil:** `Omitir por ahora` (no bloqueante para cumplir políticas de Play Store).
* **Impacto esperado:**
  * Entre el **70% y el 80% de los usuarios nuevos** cargan su contacto inmediatamente.
  * Al presionar *Guardar*, se dispara automáticamente el **Paywall de Bienvenida de 7 días gratis**:  
    > *"¡[Nombre del Contacto] ya está guardado! Activá tus 7 días gratis para que [Nombre] reciba automáticamente tus alertas oficiales por WhatsApp sin pagar nada hoy."*

---

### Opción 2: Pop-up al hacer el Primer "Estoy OK" (Si no tiene contacto)
* **Ubicación:** En `PanelScreen.kt` al presionar el botón central "Estoy OK".
* **Condición:** Si `emergencyContacts.isEmpty()`.
* **Comportamiento:**
  * En lugar del diálogo genérico de éxito, se muestra un diálogo de advertencia amigable:
    > **"¡Bienestar confirmado! Pero tu escudo está incompleto"**  
    > *"Tu registro diario quedó guardado, pero no tenés a nadie configurado para avisarle por WhatsApp si un día no podés responder. Agregá tu contacto ahora para activar tus alertas."*  
    > Botón: `[ Agregar Contacto Ahora 👥 ]`
  * Al tocar el botón, se abre directamente el formulario de carga de contacto.

---

## 📋 Lista de Tareas para la Próxima Versión (v1.1.4 / Compilación 22)

1. [ ] Crear la pantalla/paso `OnboardingContactStep.kt` en el flujo de Onboarding de Jetpack Compose.
2. [ ] Conectar la llamada a la API `POST /api/emergency-contacts` al finalizar dicho paso.
3. [ ] Encadenar el `WelcomeSoftPaywallDialog` al guardar el contacto exitosamente en el Onboarding.
4. [ ] Agregar la validación en `PanelScreen.kt` para mostrar el diálogo de "Escudo incompleto" en el primer check-in sin contactos.
5. [ ] Incrementar a `versionCode = 22` y `versionName = "1.1.4"`.
