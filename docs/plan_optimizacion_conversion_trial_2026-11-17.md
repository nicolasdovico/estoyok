# Plan Maestro de Conversión: Activación Temprana y Suscripción a Prueba Gratuita (Trial 7 Días)

**Proyecto:** Estoy Ok: Localizador Familiar  
**Fecha de Creación:** 17 de Noviembre de 2026  
**Objetivo Primario:** Aumentar la tasa de activación de la prueba gratuita de 7 días (Free Trial de Google Play Billing) en los usuarios nuevos adquiridos por Meta Ads, pasando del ~1,1% actual a una meta del **5% - 8%**.

---

## 🧭 Diagnóstico de Situación (Línea Base al 17/11/2026)

* **Adquisición Meta Ads (10 al 17 de Noviembre):**
  * 140 instalaciones reportadas en Meta a un CPI promedio récord de **$189,78 ARS**.
  * 94 usuarios de campaña en base de datos (#42 al #135).
  * 87 usuarios con token FCM nativo activo (**92,6%** de retención técnica de notificaciones).
* **Embudo de Conversión Histórico:**
  * **Check-in Diario ("Estoy OK"):** 10 usuarios (**10,6%**).
  * **Contactos de Emergencia Cargados:** 2 usuarios (**2,1%**).
  * **Activación de Prueba Gratuita de 7 Días:** 1 usuario (**1,1%** — Luciano Enrique Amarillo #102).
* **Causa Raíz Identificada:**
  1. El Paywall actual se muestra en frío inmediatamente después del carrusel inicial, antes de que el usuario experimente el valor del producto (escape automático por *"Continuar con versión básica"*).
  2. Miedo al cobro o fricción de suscripción por falta de una garantía visual clara del modelo de Google Play ($0.00 hoy, recordatorio previo y cancelación en 1 toque).
  3. Los recordatorios push automáticos esperan **6 horas** para el primer contacto, momento en el cual el usuario ya se enfrió o desinstaló la app.

---

## 🛠️ Plan de Cambios Técnicos y de Producto (3 Pilares)

### Pilar 1: Rediseño Visual del Paywall (`WelcomeSoftPaywallDialog.kt`) — "Prueba sin Riesgo"
* **Línea de Tiempo Visual (Timeline de Google Play):**
  * 🟢 **Hoy:** Acceso total Premium por **$0.00** (Todas las funciones desbloqueadas).
  * 🔔 **Día 5:** Recordatorio por notificación push de que tu prueba está por finalizar.
  * 🛡️ **Día 7:** Comienza el plan elegido, o **cancelás antes con 1 toque en Google Play** y no pagás ni un centavo.
* **Micro-copy de Confianza:**
  * Refuerzo de insignias: *"Garantizado por Google Play Store"*, *"Sin cargos sorpresa"*.
  * Selector de planes claro (Anual con 40% OFF preseleccionado vs Mensual).

### Pilar 2: Gatillo Contextual ("Momento Ajá") al Guardar Contacto de Emergencia
* **Disparador Psicológico de Alta Intención:**
  * El usuario que agrega a su familiar o contacto de seguridad (`ManageContactsModal.kt` o `PanelScreen.kt`) tiene la mayor disposición emocional a protegerlo.
  * Al completar con éxito el guardado del primer contacto de emergencia, si el usuario no tiene suscripción activa ni ha usado el trial, **se abre automáticamente el diálogo del Paywall de 7 Días Gratis**:
    > *"¡[Nombre del Contacto] ya está guardado! Para que reciba tus alertas automáticas por WhatsApp con tu mapa en tiempo real si algún día no confirmás tu bienestar, activá tus 7 Días de Regalo."*

### Pilar 3: Aceleración de Nudges y Ciclo de Nutrición (Backend Laravel)
* **Reducción de Ventana de Rescate en `SendActivationNudges.php`:**
  * **Nudge 1 (Primer Check-in):** Reducido de 6 horas a **45 minutos - 1 hora** posterior al registro. Si el usuario se registró y aún no tocó el botón central, recibe el push de bienvenida mientras aún tiene el teléfono en la mano o recuerda la descarga.
  * **Nudge 2 (Contacto de Emergencia):** A las **12 horas** (en lugar de 24 horas).
  * **Nudge 3 (Oferta Trial 7 Días):** A las **24 horas** (en lugar de 48 horas).
* **Gestión Inteligente de Tokens Huérfanos (`NotRegistered`):**
  * Al detectar el error `NotRegistered` de Firebase, limpiar automáticamente el campo `expo_push_token = null` para no saturar la cola y mantener métricas 100% limpias de usuarios con la app instalada.
* **Envío Automatizado de Email de Bienvenida (Resend):**
  * Programar en el scheduler el envío de `WelcomeTrialOfferMail.php` a las **2 horas** de la creación de la cuenta para aquellos usuarios que no hayan abierto la app.

---

## 📊 Grilla de Medición y Control de Resultados (Cohorte de Prueba)

Para medir con rigor científico la efectividad de estos cambios, se evaluará una **cohorte de 7 días** a partir de la publicación de la nueva versión (Versión 21), comparándola contra la línea base histórica:

### Tabla Comparativa de Rendimiento (Muestra a 7 Días)

| Métrica del Embudo | Línea Base (v18-v20) | Muestra Post-Cambio (Día +7) | Variación (%) | Meta Esperada | Estado |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Nuevos Registros (Cohorte)** | 94 usuarios | *(A completar en 7 días)* | — | ~70 - 100 usuarios | ⏳ Pendiente |
| **Primer Check-in ("Estoy OK")** | 10 (10,6%) | *(A completar)* | — | **> 25%** | ⏳ Pendiente |
| **Contactos de Emergencia Cargados** | 2 (2,1%) | *(A completar)* | — | **> 12%** | ⏳ Pendiente |
| **Activaciones de Trial 7 Días** | 1 (1,1%) | *(A completar)* | — | **> 5,0%** (4 a 6 trials) | ⏳ Pendiente |
| **Costo por Trial Activado (CPA Trial)** | ~$24.600 ARS | *(A completar)* | — | **< $5.000 ARS** | ⏳ Pendiente |
| **Tasa de Retención de Token (Día 3)** | ~40% (estimado) | *(A completar)* | — | **> 60%** | ⏳ Pendiente |

---

## 🛡️ Plan de Rollback y Contingencia (Por si algo falla)

1. **Aislamiento en App Móvil:**
   - Si el gatillo del Paywall al agregar contactos resultara intrusivo o reportara algún inconveniente, se puede desactivar con un flag booleano sin alterar la creación normal de contactos (`contactsRepository.createContact` permanece desacoplado).
2. **Control en Backend:**
   - Los cambios de cadencia horaria en `SendActivationNudges.php` están protegidos por deduplicación en caché (`activation_nudge_{step}_user_{id}`) con TTL de 14 días, garantizando que **ningún usuario reciba más de 1 notificación del mismo tipo**.
3. **Persistencia en Git:**
   - Cada fase se empaqueta en commits lógicos convencionales (`feat(paywall): ...`, `feat(nudges): ...`) permitiendo un `git revert` instantáneo y limpio si fuera necesario.

---

## 🚦 Próximo Paso
El plan se encuentra formulado y listo para ser ejecutado. **Aguardando la confirmación y el OK del usuario para iniciar la implementación.**
