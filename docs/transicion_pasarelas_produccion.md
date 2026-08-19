# 🚀 Guía de Transición de Pasarelas de Pago a Producción (Pase a En Vivo)

Esta guía documenta el procedimiento paso a paso para migrar las pasarelas de pago (**Stripe**, **Mercado Pago** y **PayPal**) del entorno de pruebas (*Sandbox / Test Mode*) a **Producción Real (*Live Mode*)** una vez finalizada la etapa de evaluación de 14 días en Google Play.

> [!IMPORTANT]
> **Arquitectura Zero-Code-Change:**
> El código fuente de la App Móvil (Kotlin) y del Backend (Laravel) está 100% preparado para producción. **No se requiere compilar una nueva versión de la app ni modificar código.** Toda la transición se realiza actualizando las **Variables de Entorno en Railway**.

---

## 📑 Índice de Contenidos
1. [Paso 1: Configuración de Stripe (Tarjetas y Google Pay)](#1-configuración-de-stripe-tarjetas-y-google-pay)
2. [Paso 2: Configuración de Mercado Pago (Argentina / Latam)](#2-configuración-de-mercado-pago-argentina--latam)
3. [Paso 3: Configuración de PayPal (Internacional)](#3-configuración-de-paypal-internacional)
4. [Paso 4: Tabla Maestra de Variables para Railway](#4-tabla-maestra-de-variables-para-railway)
5. [Paso 5: Liquidación de Fondos y Cobros a tu Cuenta Bancaria](#5-liquidación-de-fondos-y-cobros-a-tu-cuenta-bancaria)
6. [Paso 6: Verificación y Test Final en Vivo](#6-verificación-y-test-final-en-vivo)

---

## 1. Configuración de Stripe (Tarjetas y Google Pay)

Stripe gestiona los pagos internacionales con tarjetas de crédito/débito y Google Pay.

### 1.1 Activar Modo En Vivo
1. Ingresa a tu [Dashboard de Stripe](https://dashboard.stripe.com/).
2. En la parte superior, desactiva el interruptor **"Test mode"** para operar en **Live Mode** (si es tu primera vez, completa la verificación de identidad y cuenta bancaria requerida por Stripe).

### 1.2 Crear los Productos y Precios en Vivo
En el menú lateral **Catálogo > Productos** (*Products*):
1. Crea un producto llamado **`Estoy Ok PRO`**.
2. **Precio Mensual:**
   * **Monto:** `$4.99 USD` (o el equivalente deseado).
   * **Facturación:** Recurrente Mensual (*Recurring Monthly*).
   * Copia el **Price ID** generado (inicia con `price_`).
3. **Precio Anual (con descuento):**
   * **Monto:** `$35.99 USD` (Ahorro del 40%).
   * **Facturación:** Recurrente Anual (*Recurring Yearly*).
   * Copia el **Price ID** generado (inicia con `price_`).

### 1.3 Obtener Claves API de Producción
En **Desarrolladores > Claves de API** (*Developers > API Keys*):
1. Copia la **Publishable key** de producción (inicia con `pk_live_`).
2. Genera y copia la **Secret key** de producción (inicia con `sk_live_`).

### 1.4 Registrar el Webhook de Producción
En **Desarrolladores > Webhooks**:
1. Clic en **Añadir endpoint** (*Add Endpoint*).
2. **URL del endpoint:** `https://api.estoyok24.com/api/webhooks/stripe`
3. **Eventos a escuchar:**
   * `customer.subscription.created`
   * `customer.subscription.updated`
   * `customer.subscription.deleted`
   * `invoice.payment_succeeded`
   * `invoice.payment_failed`
4. Guarda el endpoint y haz clic en **Revelar secreto para la firma** (*Signing Secret*). Copia la clave (inicia con `whsec_`).

---

## 2. Configuración de Mercado Pago (Argentina / Latam)

Mercado Pago permite a los usuarios pagar con dinero en cuenta de Mercado Pago, Débito local y Tarjetas de Crédito nacionales.

1. Ingresa al [Panel de Desarrolladores de Mercado Pago](https://www.mercadopago.com/developers/panel/app).
2. Selecciona tu aplicación oficial de **Estoy Ok**.
3. En el menú lateral izquierdo, haz clic en **Credenciales de Producción**.
4. Completa la homologación de negocio básica si el panel te lo solicita (categoría de la app, sitio web comercial: `https://estoyok24.com`).
5. Copia las credenciales oficiales de producción:
   * **Public Key:** Inicia con `APP_USR-`.
   * **Access Token:** Inicia con `APP_USR-`.

---

## 3. Configuración de PayPal (Internacional)

*(Opcional para cobros globales adicionales)*

1. Ingresa a [PayPal Developer Dashboard](https://developer.paypal.com/dashboard/applications/live).
2. Asegúrate de estar en la pestaña **Live** (no Sandbox).
3. Selecciona tu aplicación (o créala si no existe).
4. Copia:
   * **Client ID**
   * **Secret**
   * **App ID** (mostrado en la parte superior).

---

## 4. Tabla Maestra de Variables para Railway

Entra a tu proyecto en **Railway** $\rightarrow$ Servicio **`backend`** $\rightarrow$ Pestaña **Variables**, y actualiza las siguientes claves con los valores de producción obtenidos:

```env
# ==========================================
# 💳 PASARELA STRIPE (LIVE PRODUCTION)
# ==========================================
STRIPE_KEY=<TU_STRIPE_PUBLISHABLE_KEY_PRODUCCION>
STRIPE_SECRET=<TU_STRIPE_SECRET_KEY_PRODUCCION>
STRIPE_WEBHOOK_SECRET=<TU_STRIPE_WEBHOOK_SIGNING_SECRET>
STRIPE_PRICE_ID_MONTHLY=<TU_STRIPE_PRICE_ID_MENSUAL>
STRIPE_PRICE_ID_ANNUAL=<TU_STRIPE_PRICE_ID_ANUAL>

# ==========================================
# 💙 PASARELA MERCADO PAGO (LIVE PRODUCTION)
# ==========================================
MERCADOPAGO_PUBLIC_KEY=<TU_MERCADOPAGO_PUBLIC_KEY_PRODUCCION>
MERCADOPAGO_ACCESS_TOKEN=<TU_MERCADOPAGO_ACCESS_TOKEN_PRODUCCION>

# ==========================================
# 💛 PASARELA PAYPAL (LIVE PRODUCTION)
# ==========================================
PAYPAL_MODE=live
PAYPAL_LIVE_CLIENT_ID=<TU_PAYPAL_CLIENT_ID_PRODUCCION>
PAYPAL_LIVE_CLIENT_SECRET=<TU_PAYPAL_CLIENT_SECRET_PRODUCCION>
PAYPAL_LIVE_APP_ID=<TU_PAYPAL_APP_ID_PRODUCCION>
```

> [!NOTE]
> Al guardar los cambios en Railway, el servicio `backend` y el `worker` se **redesplegarán automáticamente en ~60 segundos** aplicando la nueva configuración en caliente.

---

## 5. Liquidación de Fondos y Cobros a tu Cuenta Bancaria

| Pasarela | ¿Dónde se recibe el dinero? | ¿Cómo llega a tu cuenta bancaria (Payout)? |
| :--- | :--- | :--- |
| **Stripe** | Saldo en Stripe Dashboard. | **Transferencias automáticas bancarias periódicas** (CBU/ALIAS en ARS o cuenta en USD/SWIFT) programadas semanal o mensualmente. |
| **Mercado Pago** | Saldo disponible en tu cuenta de Mercado Pago. | **Inmediato.** Puedes transferir con 1 clic a cualquier cuenta bancaria (CBU) o billetera virtual (CVU) sin comisión, o dejarlo generando rendimientos en Mercado Fondo. |
| **PayPal** | Saldo en cuenta PayPal Business. | **Retiro bajo demanda** hacia tu cuenta bancaria asociada o mediante plataformas de retiro locales (Nubi, Macro, etc.). |

---

## 6. Verificación y Test Final en Vivo

Una vez aplicadas las variables:
1. Abre la app Estoy Ok en tu celular o ingresa a [`https://estoyok24.com`](https://estoyok24.com).
2. Ve a la pestaña **Premium** y pulsa *"Comenzar Prueba Gratis (7 Días)"*.
3. Verifica que la pasarela redirija a los entornos oficiales seguros en vivo (`checkout.stripe.com` o `mercadopago.com.ar`).
4. Al ingresar una tarjeta real, el sistema procesará la suscripción con $0.00 hoy (Trial 7 días), el webhook confirmará la operación y la app se actualizará instantáneamente a **"¡Eres Socio Premium! ⭐"**.
