# Plan Maestro de Conversión a Suscriptores Premium (Estoy Ok)

**Fecha:** 9 de Noviembre de 2026  
**Objetivo Primario:** Convertir los primeros 1 a 5 suscriptores de pago / trial de 7 días activo de Google Play Store ($4.99 USD/mes o anual con 40% OFF) sobre la base de usuarios adquiridos vía Meta Ads y Google Play.

---

## 1. Diagnóstico Actual: Por qué hay volumen pero no suscriptores aún

Actualmente la adquisición está funcionando con métricas sólidas:
* **32 instalaciones en tienda** y **38 usuarios registrados** en base de datos.
* **Costo de adquisición muy bajo:** CPI ~$770 ARS (~$0.60 USD) y CAC Registro ~$1.450 ARS (~$1.15 USD).
* **Ficha de Play Store convirtiendo al 9.43%** (excelente estándar).

**El cuello de botella de monetización:**
1. **El Soft Paywall se muestra demasiado temprano:** Se dispara una sola vez post-onboarding cuando el usuario todavía no conoce la app ni tiene confianza. La opción *"Continuar con versión básica gratuita"* es elegida por el 100%.
2. **Falta de contactos agregados (The Value Barrier):** Si el usuario no tiene contactos de seguridad agregados, el valor percibido de las alertas automáticas de WhatsApp es cero. Casos testigo reales en producción:
   * **Cristina Zarate (#53):** Realizó su check-in diario y disparó una alerta de inactividad, pero no tiene contactos de emergencia cargados (`emergency_contacts: []`).
   * **Osvaldo Rueda (#52):** Confirmó su bienestar diario y probó el flujo de inactividad, pero tampoco tiene contactos asociados.
   * **pali (#56) y Gabriela Morelli (#55):** Usuarios nuevos de alta intención que incluso registraron su número de teléfono celular argentino (+54 9 11...), pero aún no cargaron a quién cuidar ni a quién avisar.
3. **Invisibilidad de la oferta:** No existe una llamada a la acción permanente o contextual en la pantalla principal para activar los 7 días gratis con 1 toque.

---

## 2. Los 3 Pilares del Plan (Inspirado en Life360, Strava y Duolingo)

```
       [ PILAR 1: PUBLICIDAD Y MENSAJE DE VENTA ]
           Publicidad orientada al dolor familiar crítico
           (Tranquilidad mental y SOS de auxilio por WhatsApp)
                                ⬇
       [ PILAR 2: EL "MOMENTO AJÁ" IN-APP ]
           Guiar al usuario a cargar su 1er contacto y
           disparar el WhatsApp de prueba ("Efecto WhatsApp")
                                ⬇
       [ PILAR 3: PAYWALL CONTEXTUAL Y BANNER PERMANENTE ]
           Banner "7 Días Gratis" en el Panel Principal +
           Paywalls en momentos de alto deseo (rutas, audio SOS)
```

---

## 3. Pilar 1: Optimización de la Publicidad (Meta Ads) para Atraer Usuarios con Intención de Pago

### A. Creativo Ganador con Ángulo de Tranquilidad (Reel 2 y Nuevo Reel de Respaldo)
* **El público que más paga suscripciones en apps familiares:**
  1. Hijos de 30 a 55 años preocupados por sus padres mayores que viven solos.
  2. Padres de adolescentes que salen solos de noche.
* **Ajuste de segmentación en Meta Ads Manager:**
  * Segmentar edad mínima de **30 a 58 años** (quienes tienen poder adquisitivo y tarjeta de crédito vinculada a Google Play).
  * En la campaña actual, **equilibrar el presupuesto:** Asignar un límite de gasto mínimo al **Reel 2 (Vivir Solo)** para que no sea canibalizado por el Reel 1.
* **Nuevo Gancho Publicitario (Copy & Creativo):**
  * *"Por menos de lo que cuesta un café al mes ($4.99), tu familiar tiene un botón de auxilio que te manda un WhatsApp con su ubicación exacta y audio en vivo si algo le pasa."*

---

## 4. Pilar 2: Provocar el "Momento Ajá" en los Usuarios Existentes

Para que un usuario pague, primero tiene que ver el bot de WhatsApp funcionando con alguien de su familia.

### Acciones Directas sobre la Base Existente (38 Usuarios):
1. **Rescate Personalizado por Push FCM y Email:**
   * Enviar un correo o push directo a los usuarios activos reales que aún no tienen contacto (ej. Cristina Zarate #53, Osvaldo Rueda #52, pali #56, Gabriela Morelli #55):
     * *Asunto:* `🛡️ [Nombre], activá a quién te cuidará en Estoy Ok (7 días de WhatsApp gratis)`
     * *Cuerpo:* *"Vemos que confirmás tu bienestar a diario. Para que el sistema funcione en caso de que un día no puedas responder, agregá a tu contacto de confianza y probalo gratis por 7 días."*
2. **Burbuja de Acción Rápida en el Panel:**
   * Si el usuario no tiene contactos, destacar con pulsación suave el botón: *"🔔 Agregar a quién avisar por WhatsApp"*.

---

## 5. Pilar 3: Modificaciones Clave en la App (Android Nativo)

### A. Banner Dorado Permanente en `PanelScreen.kt`
Agregar una tarjeta elegante debajo del botón central "Estoy OK" (visible solo si `!is_premium`):
* **Visual:** Borde sutil dorado/esmeralda, icono de corona 👑.
* **Texto:**
  * **Título:** *"Tenés 7 días de Estoy Ok Premium Gratis"*
  * **Subtexto:** *"Avisos ilimitados por WhatsApp a tus seres queridos y SOS con audio ambiental. $0.00 hoy."*
  * **Botón CTA:** `[ Iniciar Prueba de 7 Días ]` $\rightarrow$ Abre directamente el flujo nativo de Google Play Billing 8.0.

### B. Paywall Contextual por Función Bloqueada
* Si el usuario toca el botón de **Historial de Movimientos / Rutas** en el mapa $\rightarrow$ Se abre el Paywall explicando que el histórico de 30 días es una función Premium con prueba gratuita.
* Si el usuario intenta agregar un **2º o 3º contacto de seguridad** $\rightarrow$ Se abre el Paywall destacando protección para toda la familia.

### C. Contador de Racha de Bienestar ("Streak" Duolingo)
* Mostrar en el Panel: *"🔥 X días consecutivos confirmando que estás OK"*.
* Reforzar la aversión a la pérdida: si el usuario no tiene Premium, sus contactos no se enterarán automáticamente si rompe la racha.

---

## 6. Cronograma de Ejecución y Métricas de Éxito

| Etapa | Acción | Responsable / Entorno | Plazo |
| :--- | :--- | :--- | :--- |
| **Paso 1** | Lanzamiento v18 con Meta SDK (Instalaciones registradas en Ads Manager) | Google Play Console (En revisión) | Inmediato (Hoy) |
| **Paso 2** | Equilibrar entrega de presupuesto Meta Ads (Reel 2 vs Reel 1) | Meta Ads Manager | Inmediato (Hoy) |
| **Paso 3** | Implementar Banner Dorado de 7 Días Gratis en `PanelScreen.kt` | Android Nativo (`dev`) | 24 - 48 hs |
| **Paso 4** | Disparar ciclo de empuje por email/push a los 38 usuarios actuales | Backend Laravel (`main`) | 48 hs |
| **Paso 5** | Medir los primeros trials activos en Google Play y base de datos | Producción | Fin de semana |

### Métrica de Éxito:
* Lograr entre **1 y 3 usuarios en estado `trialing` o `active`** en Google Play Billing en los próximos 7 a 10 días.
