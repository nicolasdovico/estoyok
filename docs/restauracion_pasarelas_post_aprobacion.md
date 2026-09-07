# 🔄 Guía de Restauración de Pasarelas de Pago Post-Aprobación en Google Play

Este documento contiene el procedimiento exacto paso a paso y los bloques de código necesarios para **volver a habilitar las pasarelas de pago externas (Stripe, Mercado Pago y PayPal)** en la App Móvil Android y el Backend, una vez que el equipo de revisión de Google Play haya **aprobado la solicitud de paso a Producción Abierta**.

---

## 📌 1. Contexto

Durante la revisión humana de Google Play para el paso a Producción tras los 14 días de Pruebas Cerradas, se activó el **Modo de Revisión Segura (v7 / 1.0.4)**:
* Se removieron temporalmente los textos y botones de pasarelas externas (*Stripe, Mercado Pago, PayPal*) en la app para evitar que Google rechace la aplicación por la *Política de Pagos (Google Play Billing Policy)*.
* El botón *"Activar Prueba Gratis (7 Días)"* activa el trial directamente en la base de datos sin pedir tarjeta de crédito.

**Toda la infraestructura de pasarelas quedó 100% preservada e intacta en el código:**
* En el backend: el método `checkout()` en `SubscriptionController.php` con soporte para Stripe, Mercado Pago y PayPal sigue existiendo y funcionando.
* En la app móvil: la función `startTrialAndCheckout()` en `FamiliaViewModel.kt` sigue existiendo y lista para usarse.
* En Git: Los commits de referencia donde estaba todo visible son:
  * Backend: `99d3ab7` (y anterior)
  * App Móvil: `322e71a` (y anterior `718023d`)

---

## 🛠️ 2. Paso a Paso para Restaurar las Pasarelas

Una vez que Google Play confirme la aprobación a Producción:

### Paso 2.1: Restaurar el Backend (Laravel API)

En el archivo `backend/app/Http/Controllers/Api/SubscriptionController.php`, dentro de la función `startTrial()`:

**Reemplazar:**
```php
    public function startTrial(Request $request, MercadoPagoService $mpService, PayPalService $paypalService)
    {
        $user = Auth::user();

        if ($user->subscription_status === 'active' && ($user->is_premium || $user->subscribed('default'))) {
            return response()->json([
                'message' => 'Ya cuentas con una suscripción Premium activa.'
            ], 422);
        }

        // Direct in-app 7-day trial activation (Google Play compliance & safe review mode)
        $user->update([
            'is_premium' => true,
            'subscription_status' => 'trialing',
            'subscription_provider' => 'trial',
            'trial_ends_at' => now()->addDays(7),
        ]);

        return response()->json([
            'message' => '¡Prueba gratuita de 7 días activada con éxito! Disfruta de Estoy Ok PRO.',
            'checkout_url' => null,
            'user' => $user->fresh(),
        ]);
    }
```

**Por la llamada original a checkout:**
```php
    public function startTrial(Request $request, MercadoPagoService $mpService, PayPalService $paypalService)
    {
        $user = Auth::user();

        if ($user->subscription_status === 'active' && ($user->is_premium || $user->subscribed('default'))) {
            return response()->json([
                'message' => 'Ya cuentas con una suscripción Premium activa.'
            ], 422);
        }

        return $this->checkout($request, $mpService, $paypalService);
    }
```

---

### Paso 2.2: Restaurar la App Móvil Android (Kotlin Compose)

En el archivo `android-native/app/src/main/java/com/estoyok/app/features/tracking/presentation/PremiumScreen.kt`:

1. **Re-incorporar la variable de estado:**
   Debajo de `val scrollState = rememberScrollState()`:
   ```kotlin
   var selectedPayProvider by remember { mutableStateOf("stripe") }
   ```

2. **Re-insertar el selector de pasarelas visuales:**
   Justo debajo del bloque de `BillingPlanCard` (selector Anual vs Mensual) y antes del botón CTA:
   ```kotlin
   Spacer(modifier = Modifier.height(22.dp))

   // Payment Provider Selector
   Text(
       text = "🏦 Elige tu Medio de Pago:",
       fontSize = 12.sp,
       fontWeight = FontWeight.Bold,
       color = TextPrimary,
       modifier = Modifier.align(Alignment.Start)
   )
   Spacer(modifier = Modifier.height(8.dp))
   Column(
       verticalArrangement = Arrangement.spacedBy(8.dp),
       modifier = Modifier.fillMaxWidth()
   ) {
       listOf(
           Triple("stripe", "💳 Tarjeta de Crédito / Débito", "Procesamiento seguro internacional (Visa, Mastercard, Amex)"),
           Triple("mercadopago", "Mercado Pago", "Suscripción mensual en pesos (ARS) para Argentina"),
           Triple("paypal", "PayPal", "Débito automático en dólares (USD) para el resto del mundo")
       ).forEach { (id, label, subtext) ->
           val selected = selectedPayProvider == id
           Card(
               modifier = Modifier
                   .fillMaxWidth()
                   .clickable { selectedPayProvider = id },
               shape = RoundedCornerShape(12.dp),
               colors = CardDefaults.cardColors(
                   containerColor = if (selected) PrimaryEmerald.copy(alpha = 0.15f) else DarkSurfaceVariant
               ),
               border = BorderStroke(
                   width = if (selected) 2.dp else 1.dp,
                   color = if (selected) PrimaryEmerald else DarkSurfaceVariant
               )
           ) {
               Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(horizontal = 14.dp, vertical = 12.dp),
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   RadioButton(
                       selected = selected,
                       onClick = { selectedPayProvider = id },
                       colors = RadioButtonDefaults.colors(
                           selectedColor = PrimaryEmerald,
                           unselectedColor = TextMuted
                       )
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   Column {
                       Text(
                           text = label,
                           color = if (selected) PrimaryEmerald else TextPrimary,
                           fontSize = 13.sp,
                           fontWeight = FontWeight.Bold
                       )
                       Text(
                           text = subtext,
                           color = TextSecondary,
                           fontSize = 10.5.sp,
                           lineHeight = 14.sp
                       )
                   }
               }
           }
       }
   }
   ```

3. **Re-conectar el botón principal para abrir la pasarela:**
   Reemplazar el botón CTA por:
   ```kotlin
   // CTA Button
   Button(
       onClick = {
           viewModel.startTrialAndCheckout(selectedPayProvider) { checkoutUrl ->
               try {
                   val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                   context.startActivity(intent)
               } catch (e: Exception) {
                   Toast.makeText(context, "Error al abrir enlace de pago: ${e.message}", Toast.LENGTH_LONG).show()
               }
           }
       },
       modifier = Modifier
           .fillMaxWidth()
           .height(52.dp),
       enabled = !viewModel.checkoutLoading,
       shape = RoundedCornerShape(12.dp),
       colors = ButtonDefaults.buttonColors(
           containerColor = PrimaryEmerald,
           contentColor = TextOnPrimary
       )
   ) {
       if (viewModel.checkoutLoading) {
           CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextOnPrimary)
       } else {
           Column(horizontalAlignment = Alignment.CenterHorizontally) {
               Text(
                   text = "Iniciar Prueba Gratis (7 Días)",
                   fontWeight = FontWeight.ExtraBold,
                   fontSize = 15.sp,
                   color = TextOnPrimary
               )
               Text(
                   text = "Hoy $0.00 • Cancela en cualquier momento",
                   fontSize = 10.sp,
                   color = TextOnPrimary.copy(alpha = 0.85f)
               )
           }
       }
   }
   ```

---

### Paso 2.3: Configurar las Claves de Producción en Railway

Asegúrate de haber ingresado en Railway las credenciales reales según la guía maestra `docs/transicion_pasarelas_produccion.md`:
* `STRIPE_KEY=pk_live_...`
* `STRIPE_SECRET=sk_live_...`
* `MERCADOPAGO_ACCESS_TOKEN=APP_USR-...`
* `PAYPAL_MODE=live`

---

### Paso 2.4: Compilar Versión 8 y Desplegar a Producción

1. Incrementar en `android-native/app/build.gradle.kts`:
   ```kotlin
   versionCode = 8
   versionName = "1.0.5"
   ```
2. Ejecutar la compilación del bundle:
   ```bash
   cd android-native && ./gradlew bundleRelease
   ```
3. Subir el bundle `app-release.aab` generado directamente a la pista de **Producción** en Google Play Console.
