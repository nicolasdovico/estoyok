package com.estoyok.app.features.tracking.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun PremiumScreen(
    viewModel: FamiliaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedPayProvider by remember { mutableStateOf("stripe") }
    var selectedBillingCycle by remember { mutableStateOf("monthly") } // "monthly" vs "annual"
    val isPremium = viewModel.user?.isUserPremium == true

    LaunchedEffect(isPremium) {
        if (isPremium) {
            scrollState.animateScrollTo(0)
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp, top = 50.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Title
            Text(
                text = "👑 Suscripción Premium",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Subscription status card or Hero Paywall Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) DarkSurfaceVariant else Color(0xFF0C241D)
                ),
                border = BorderStroke(1.5.dp, PrimaryEmerald)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isPremium) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "¡Eres Socio Premium! ⭐",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tu núcleo familiar cuenta con la máxima protección: alertas ilimitadas por WhatsApp/SMS, S.O.S con grabación de audio ambiental, telemetría vehicular e historial de 30 días.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    } else {
                        // Hero Header
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✨ OFERTA EXCLUSIVA DE LANZAMIENTO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Prueba Estoy Ok PRO gratis por 7 días 👑",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Protección familiar sin límites. Desbloquea SOS ambiental, telemetría vehicular y rastro completo.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 7-Day Trial Timeline Breakdown Card
                        Text(
                            text = "📅 ¿Cómo funciona tu prueba gratis?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TimelineStepItem(
                                number = "1",
                                title = "Hoy (Día 1)",
                                subtitle = "Acceso inmediato e ilimitado a todas las funciones PRO por $0.00.",
                                isFirst = true
                            )
                            TimelineStepItem(
                                number = "5",
                                title = "Día 5 (Recordatorio)",
                                subtitle = "Te enviamos una notificación push. Puedes cancelar en 1 toque sin cobro.",
                                isFirst = false
                            )
                            TimelineStepItem(
                                number = "7",
                                title = "Día 7 (Final del Trial)",
                                subtitle = if (selectedBillingCycle == "annual") "Comienza la facturación anual ($35.99/año • $2.99/mes)." else "Comienza la facturación mensual ($4.99/mes).",
                                isFirst = false
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Billing Cycle Selector (Monthly vs Annual 40% OFF)
                        Text(
                            text = "💳 Selecciona tu Plan:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BillingPlanCard(
                                title = "Anual (Ahorra 40%)",
                                price = "$2.99",
                                unit = "/ mes",
                                detail = "$35.99 facturado al año",
                                isSelected = selectedBillingCycle == "annual",
                                badgeText = "OFERTA LANZAMIENTO 🔥",
                                onClick = { selectedBillingCycle = "annual" },
                                modifier = Modifier.weight(1f)
                            )
                            BillingPlanCard(
                                title = "Mensual",
                                price = "$4.99",
                                unit = "/ mes",
                                detail = "Facturación mensual flexible",
                                isSelected = selectedBillingCycle == "monthly",
                                badgeText = null,
                                onClick = { selectedBillingCycle = "monthly" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Payment Provider Selector (Option B - Clear User-Friendly Labels)
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

                        Spacer(modifier = Modifier.height(22.dp))

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
                    }
                }
            }

            // Feature Comparison Matrix (Free vs PRO)
            Text(
                text = "📊 Comparativa de Planes (Free vs PRO)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Característica",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = "Free 🛡️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "PRO 👑",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryEmerald,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = CardBackground)

                    // Matrix Items
                    val featureMatrix = listOf(
                        Triple("Historial de Recorridos", "24 Horas", "30 Días"),
                        Triple("Rastreo GPS en Vehículo", "Estándar (30s)", "Alta Frecuencia (5s)"),
                        Triple("Zonas Seguras (Geocercas)", "Hasta 2", "Ilimitadas"),
                        Triple("Alertas por WhatsApp", "Desactivado", "Ilimitadas"),
                        Triple("S.O.S. con Audio Ambiente", "Solo Push", "Grabación 15s + 5s GPS"),
                        Triple("Detección de Accidentes (Crash)", "Desactivado", "Acelerómetro + Sirena"),
                        Triple("Telemetría de Conducción", "Básica", "Frenadas / Excesos / Celular"),
                        Triple("Alertas Batería Baja / Sensores", "Básica", "Prioritarias (<15%)"),
                        Triple("Radar Móvil de Proximidad", "Desactivado", "Activo")
                    )

                    featureMatrix.forEach { (feature, freeVal, proVal) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = feature,
                                fontSize = 11.5.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                text = freeVal,
                                fontSize = 10.5.sp,
                                color = TextMuted,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryEmerald.copy(alpha = 0.15f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = proVal,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEmerald,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        HorizontalDivider(color = CardBackground.copy(alpha = 0.5f))
                    }
                }
            }

            // Error banner if any
            viewModel.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "✗ $error",
                        color = PrimaryRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineStepItem(
    number: String,
    title: String,
    subtitle: String,
    isFirst: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (isFirst) PrimaryEmerald else DarkSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = if (isFirst) TextOnPrimary else PrimaryEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun BillingPlanCard(
    title: String,
    price: String,
    unit: String,
    detail: String,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryEmerald.copy(alpha = 0.15f) else DarkSurfaceVariant
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryEmerald else DarkSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            badgeText?.let { badge ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryEmerald)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextOnPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryEmerald else TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = detail,
                fontSize = 9.5.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
