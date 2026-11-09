package com.estoyok.app.features.tracking.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estoyok.app.core.theme.DarkBackground

// Colores del tema Premium Oro / Champán
private val GoldPrimary = Color(0xFFFFD700)
private val GoldAccent = Color(0xFFFFC107)
private val GoldContainerDark = Color(0xFF1E1A0E)
private val GoldBorder = Color(0x80FFD700)

/**
 * Chip flotante elegante para MapaScreen:
 * Se muestra debajo del selector de núcleos cuando el usuario no tiene suscripción activa.
 */
@Composable
fun MapTrialOfferChip(
    visible: Boolean,
    hasUsedTrial: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            shape = CircleShape,
            color = GoldContainerDark.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, GoldBorder),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "👑",
                    fontSize = 12.sp
                )
                Text(
                    text = if (hasUsedTrial) "Estoy Ok Premium" else "7 Días Premium Gratis",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = "• WhatsApp Ilimitado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Activar",
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Banner Dorado principal para PanelScreen:
 * Tarjeta visible antes o después del check-in diario para impulsar la prueba gratuita de Google Play.
 */
@Composable
fun GoldTrialPromoCard(
    visible: Boolean,
    hasUsedTrial: Boolean = false,
    onActivateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onActivateClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GoldContainerDark),
        border = BorderStroke(1.2.dp, GoldBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "👑", fontSize = 16.sp)
                        Text(
                            text = if (hasUsedTrial) "Activá Estoy Ok Premium" else "Tenés 7 Días de Regalo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (hasUsedTrial) "PREMIUM" else "GRATIS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Avisos automáticos por WhatsApp a tus contactos si un día no confirmás tu bienestar y SOS con audio ambiental.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (hasUsedTrial) "Suscripción segura en Google Play" else "$0.00 hoy • Cancelá en Google Play",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.55f)
                    )

                    Button(
                        onClick = onActivateClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (hasUsedTrial) "Suscribirme" else "Probar 7 Días",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
