package com.estoyok.app.features.tracking.presentation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*

@Composable
fun WelcomeSoftPaywallDialog(
    onDismiss: () -> Unit,
    viewModel: FamiliaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedPlan by remember { mutableStateOf("monthly") } // "monthly" vs "annual"
    val hasUsedTrial = viewModel.user?.hasUsedTrial == true

    BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                border = BorderStroke(1.5.dp, PrimaryEmerald.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header con botón de cierre sutil
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PrimaryEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "🎁 REGALO DE BIENVENIDA",
                                color = PrimaryEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Título e Ícono
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(PrimaryEmerald.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛡️", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¡Te damos la bienvenida a Estoy Ok!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Disfrutá de 7 días gratis de protección completa para vos y tu familia sin pagar nada hoy.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Beneficios clave en tarjeta
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BenefitRowItem(
                                iconText = "📲",
                                title = "Alertas automáticas por WhatsApp",
                                description = "Si no confirmás tu bienestar diario, avisamos a tus contactos con mapa de rescate."
                            )
                            BenefitRowItem(
                                iconText = "🚨",
                                title = "SOS de Bolsillo (4 toques)",
                                description = "Presioná 4 veces el botón de encendido para enviar audio de 15s y GPS en vivo."
                            )
                            BenefitRowItem(
                                iconText = "🚗",
                                title = "Protección Vehicular y Choques",
                                description = "Detección de impactos por Fuerza G y rastreo de alta frecuencia en ruta."
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector de Plan: Mensual vs Anual
                    Text(
                        text = "Elegí tu modalidad post-prueba:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PlanSelectCard(
                            title = "Anual (Ahorra 40%)",
                            price = "$2.99 / mes",
                            subtitle = "7 días gratis, luego $35.99/año",
                            isSelected = selectedPlan == "annual",
                            badge = "MEJOR VALOR 🔥",
                            onClick = { selectedPlan = "annual" },
                            modifier = Modifier.weight(1f)
                        )
                        PlanSelectCard(
                            title = "Mensual",
                            price = "$4.99 / mes",
                            subtitle = "7 días gratis, luego mensual",
                            isSelected = selectedPlan == "monthly",
                            badge = null,
                            onClick = { selectedPlan = "monthly" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Principal: Comenzar prueba gratis de 7 días
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null && viewModel.isGooglePlayBillingReady) {
                                viewModel.launchGooglePlaySubscription(activity, selectedPlan, hasUsedTrial) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                                onDismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Google Play Billing conectando... Por favor intenta en unos segundos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEmerald,
                            contentColor = TextOnPrimary
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Comenzar prueba gratis de 7 días 👑",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextOnPrimary
                            )
                            Text(
                                text = "$0.00 hoy • Cancela cuando quieras en Play Store",
                                fontSize = 10.sp,
                                color = TextOnPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Secundario: No bloqueante
                    TextButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Continuar con la versión básica gratuita",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenefitRowItem(
    iconText: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = iconText, fontSize = 18.sp)
        Column {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun PlanSelectCard(
    title: String,
    price: String,
    subtitle: String,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) PrimaryEmerald else BorderColor
                ),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryEmerald.copy(alpha = 0.12f) else CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (badge != null) {
                Surface(
                    color = PrimaryEmerald,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextOnPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Spacer(modifier = Modifier.height(15.dp))
            }

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryEmerald else TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = price,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}
