# 💳 Guía de Transición de Stripe a Producción (Pase a En Vivo)

Esta guía explica en detalle cómo migrar el sistema de cobros de **Stripe** del modo de pruebas (*Test Mode*) al entorno de **Producción (*Live Mode*)**. 

> [!IMPORTANT]
> El proyecto **Estoy Ok** ha sido diseñado bajo la arquitectura **Production-Ready**. Esto significa que **NO SE REQUIERE MODIFICAR CÓDIGO** en el backend ni en los frontends para pasar a producción. Toda la migración se realiza mediante la actualización de las claves en las variables de entorno (`.env` / Railway).

---

## 📋 Checklist de 5 Pasos para el Lanzamiento

### 1. Activar el Modo "En Vivo" en el Dashboard de Stripe
1. Inicia sesión en tu cuenta de [Stripe Dashboard](https://dashboard.stripe.com/).
2. En la barra superior, cambia el interruptor de **"Test Mode"** a **"Live Mode"** (o completa la verificación de cuenta de empresa si es tu primera vez).

---

### 2. Crear los Productos y Precios en Vivo
En el apartado **Catalog > Products** de Stripe (en Live Mode):
1. Crea un producto llamado **Estoy Ok PRO**.
2. Agrega el precio para el plan **Mensual**:
   * **Monto:** $4.99 USD (o la moneda de tu preferencia).
   * **Facturación:** Recurrente Mensual.
   * Copia el **Price ID** resultante (ej: `price_1Pxxx...`).
3. Agrega el precio para el plan **Anual**:
   * **Monto:** $35.99 USD (Facturación anual con 40% de descuento).
   * **Facturación:** Recurrente Anual.
   * Copia el **Price ID** resultante (ej: `price_1Pyyy...`).

---

### 3. Obtener las Claves de Producción (*Live API Keys*)
En el apartado **Developers > API Keys**:
1. Copia la **Publishable Key** de producción (`pk_live_...`).
2. Copia la **Secret Key** de producción (`sk_live_...`).

---

### 4. Registrar el Webhook en Producción
En el apartado **Developers > Webhooks**:
1. Haz clic en **Add Endpoint**.
2. **Endpoint URL:** `https://tu-dominio-backend.up.railway.app/api/webhooks/stripe`
3. **Eventos a escuchar:**
   * `customer.subscription.created`
   * `customer.subscription.updated`
   * `customer.subscription.deleted`
   * `invoice.payment_succeeded`
   * `invoice.payment_failed`
4. Copia el **Signing Secret** generado (`whsec_live_...`).

---

### 5. Actualizar las Variables de Entorno en el Servidor (Railway / `.env`)

Sustituye los valores de prueba por los de producción:

```env
# Stripe Live Keys
STRIPE_KEY=<tu_clave_publica_live>
STRIPE_SECRET=<tu_clave_secreta_live>
STRIPE_WEBHOOK_SECRET=<tu_webhook_secret_live>

# Stripe Live Price IDs
STRIPE_PRICE_ID_MONTHLY=<price_id_mensual_live>
STRIPE_PRICE_ID_ANNUAL=<price_id_anual_live>
```

Reinicia los servicios del backend (`docker compose restart backend worker` o despliegue automático en Railway).

---

## ✅ Verificación del Entorno en Producción
* Intenta suscribirte desde la App Móvil o la Web.
* Serás redirigido al formulario de cobro seguro en vivo de Stripe.
* Al procesarse la tarjeta real, el webhook enviará la confirmación a Laravel y la cuenta pasará a estado `is_premium = true`.
