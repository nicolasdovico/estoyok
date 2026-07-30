# 🏷️ Estrategia de Precios: Oferta de Lanzamiento vs. Tarifas Regulares

Este documento registra la estrategia de precios de suscripción para **Estoy Ok PRO**, detallando la **Oferta de Lanzamiento (vigente actualmente)** y la **Estructura Regular Futura (20% OFF)** planificada para cuando finalice la promoción inicial.

---

## 🚀 1. Oferta de Lanzamiento (Promoción Vigente)

Durante la fase de lanzamiento inicial de Estoy Ok, se ofrece un descuento agresivo de captación del **40% OFF** en la suscripción anual para acelerar la adopción y conversión inicial de usuarios.

- **Plan Mensual Flexible:** **$4.99 USD / mes**
- **Plan Anual Promocional (40% OFF):**
  - **Precio Total Anual:** **$35.99 USD / año**
  - **Equivalente Mensual:** **$2.99 USD / mes**
  - **Incentivo Visual:** Badge *"OFERTA LANZAMIENTO 🔥 (Ahorra 40%)"*

---

## 📈 2. Estructura de Precios Regulares (Futuro Post-Lanzamiento)

Una vez concluida la etapa de lanzamiento inicial, los precios se ajustarían al modelo regular estandarizado del **20% OFF**, posicionando a Estoy Ok por encima de la oferta de descuento de **Life360** (~16.6% OFF) para mantener una propuesta altamente seductora pero más sostenible.

- **Plan Mensual Flexible:** **$4.99 USD / mes**
- **Plan Anual Regular (20% OFF):**
  - **Precio Total Anual:** **$47.88 USD / año**
  - **Equivalente Mensual:** **$3.99 USD / mes**
  - **Ahorro Anual:** **$12.00 USD / año**

---

## 📊 3. Comparativa vs. Competencia (Life360)

| Servicio | Plan Mensual | Plan Anual (Total) | Equivalente Mensual | Descuento Anual |
| :--- | :--- | :--- | :--- | :--- |
| **Life360 Gold** | $14.99 / mes | **$149.99 / año** | ~$12.50 / mes | **~16.6% OFF** (2 meses gratis) |
| **Life360 Platinum** | $24.99 / mes | **$249.99 / año** | ~$20.83 / mes | **~16.6% OFF** (2 meses gratis) |
| **Estoy Ok PRO (Lanzamiento)** | $4.99 / mes | **$35.99 / año** | **$2.99 / mes** | **40.0% OFF** (~5 meses gratis) |
| **Estoy Ok PRO (Regular)** | $4.99 / mes | **$47.88 / año** | **$3.99 / mes** | **20.0% OFF** (~2.4 meses gratis) |

---

## 🛠️ 4. Guía de Transición Técnica (Paso a Paso)

Cuando se decida migrar de la *Oferta de Lanzamiento* a los *Precios Regulares*:

1. **Android Nativo (`PremiumScreen.kt`):**
   - Actualizar `price = "$3.99"`, `detail = "$47.88 facturado al año"`, `badgeText = "Ahorra 20%"` y timeline step 3.
2. **Backend (Laravel):**
   - `MercadoPagoService.php`: Cambiar `unit_price` anual de `35990.0` a `47880.0` ARS.
   - `PayPalService.php`: Vincular `PAYPAL_PLAN_ID_ANNUAL` con el valor de $47.88 USD.
   - `Stripe`: Actualizar `STRIPE_PRICE_ID_ANNUAL` en `.env` al nuevo Price ID de $47.88/año.
