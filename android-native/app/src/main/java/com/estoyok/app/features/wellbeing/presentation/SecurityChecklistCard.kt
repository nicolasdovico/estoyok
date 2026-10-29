package com.estoyok.app.features.wellbeing.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estoyok.app.core.theme.*
import kotlinx.coroutines.delay

@Composable
fun SecurityChecklistCard(
    hasCheckedIn: Boolean,
    hasContacts: Boolean,
    hasGeofences: Boolean,
    isCompletedPermanently: Boolean,
    isHiddenInSession: Boolean,
    onCheckInClick: () -> Unit,
    onAddContactClick: () -> Unit,
    onCreateGeofenceClick: () -> Unit,
    onHideForSession: () -> Unit,
    onReopenInSession: () -> Unit,
    onAllMissionsDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isCompletedPermanently) return

    val completedCount = (if (hasCheckedIn) 1 else 0) +
            (if (hasContacts) 1 else 0) +
            (if (hasGeofences) 1 else 0)

    val isAllCompleted = completedCount == 3
    val progress = completedCount / 3f

    // Auto-festejo y desaparición definitiva cuando se alcanzan las 3 misiones
    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(isAllCompleted) {
        if (isAllCompleted) {
            showCelebration = true
            delay(3500L) // 3.5 segundos de festejo visible
            onAllMissionsDone()
        }
    }

    AnimatedVisibility(
        visible = !isHiddenInSession || showCelebration,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
    ) {
        val backgroundGradient = Brush.verticalGradient(
            colors = when {
                showCelebration || isAllCompleted -> listOf(
                    PrimaryEmerald.copy(alpha = 0.20f),
                    CardBackground
                )
                completedCount == 0 -> listOf(
                    Color(0xFFE11D48).copy(alpha = 0.16f), // Advertencia suave al 0%
                    CardBackground
                )
                else -> listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    CardBackground
                )
            }
        )

        val borderColor = when {
            showCelebration || isAllCompleted -> PrimaryEmerald.copy(alpha = 0.6f)
            completedCount == 0 -> Color(0xFFF43F5E).copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        }

        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, borderColor),
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundGradient, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    when {
                                        showCelebration || isAllCompleted -> PrimaryEmerald.copy(alpha = 0.25f)
                                        completedCount == 0 -> Color(0xFFE11D48).copy(alpha = 0.25f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (showCelebration || isAllCompleted) Icons.Default.Shield else Icons.Default.Security,
                                contentDescription = null,
                                tint = when {
                                    showCelebration || isAllCompleted -> PrimaryEmerald
                                    completedCount == 0 -> Color(0xFFFB7185)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = when {
                                    showCelebration || isAllCompleted -> "¡Tu Escudo está Activo al 100%! 🎉"
                                    completedCount == 0 -> "Comenzá a protegerte (0 de 3)"
                                    else -> "Completá tu protección ($completedCount de 3)"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    showCelebration || isAllCompleted -> PrimaryEmerald
                                    completedCount == 0 -> Color.White
                                    else -> TextPrimary
                                }
                            )
                            Text(
                                text = when {
                                    showCelebration || isAllCompleted -> "¡Excelente! Todas las defensas configuradas"
                                    completedCount == 0 -> "⚠️ Nadie recibirá avisos hasta que completes esto"
                                    completedCount == 1 -> "Te faltan 2 pasos para activar las alertas"
                                    else -> "¡Casi listo! Solo falta 1 paso"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = if (completedCount == 0) FontWeight.Medium else FontWeight.Normal,
                                color = if (completedCount == 0) Color(0xFFFDA4AF) else TextMuted
                            )
                        }
                    }

                    if (!showCelebration && !isAllCompleted) {
                        IconButton(
                            onClick = onHideForSession,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Ocultar por ahora",
                                tint = TextMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barra de progreso con gradiente
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        showCelebration || isAllCompleted -> PrimaryEmerald
                        completedCount == 0 -> Color(0xFFF43F5E)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Misión 1: Check-in de bienestar
                VibrantChecklistStep(
                    stepNumber = "1",
                    title = "Confirmá tu bienestar hoy",
                    subtitle = "Tocá Estoy OK para iniciar el reloj diario",
                    isCompleted = hasCheckedIn,
                    actionLabel = "Confirmar",
                    icon = Icons.Default.Check,
                    actionButtonColor = PrimaryEmerald,
                    onClick = onCheckInClick
                )

                Spacer(modifier = Modifier.height(9.dp))

                // Misión 2: Contacto de confianza
                VibrantChecklistStep(
                    stepNumber = "2",
                    title = "Agregá a quién te cuidará",
                    subtitle = "Recibirá un WhatsApp con tu mapa si no reportás",
                    isCompleted = hasContacts,
                    actionLabel = "Agregar",
                    icon = Icons.Default.PersonAdd,
                    actionButtonColor = MaterialTheme.colorScheme.primary,
                    onClick = onAddContactClick
                )

                Spacer(modifier = Modifier.height(9.dp))

                // Misión 3: Zona segura (Casa/Colegio)
                VibrantChecklistStep(
                    stepNumber = "3",
                    title = "Creá tu primera Zona Segura",
                    subtitle = "Marcá Casa o Trabajo para avisos de llegada",
                    isCompleted = hasGeofences,
                    actionLabel = "Crear",
                    icon = Icons.Default.LocationOn,
                    actionButtonColor = MaterialTheme.colorScheme.primary,
                    onClick = onCreateGeofenceClick
                )
            }
        }
    }

    // Chip reabrible cuando el usuario la minimizó en la sesión activa y aún tiene misiones pendientes
    if (isHiddenInSession && !showCelebration && !isAllCompleted) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onReopenInSession)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (completedCount == 0) Color(0xFFFB7185) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tareas de seguridad pendientes ($completedCount de 3)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ver tareas",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VibrantChecklistStep(
    stepNumber: String,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    actionLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionButtonColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (isCompleted) PrimaryEmerald.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isCompleted) PrimaryEmerald.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCompleted, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            if (isCompleted) PrimaryEmerald else Color.White.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) PrimaryEmerald else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isCompleted) TextMuted else TextMuted.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!isCompleted) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = actionButtonColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = actionLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
