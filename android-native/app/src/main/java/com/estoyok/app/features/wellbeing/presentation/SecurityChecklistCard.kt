package com.estoyok.app.features.wellbeing.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estoyok.app.core.theme.*

@Composable
fun SecurityChecklistCard(
    hasCheckedIn: Boolean,
    hasContacts: Boolean,
    hasGeofences: Boolean,
    isDismissed: Boolean,
    onCheckInClick: () -> Unit,
    onAddContactClick: () -> Unit,
    onCreateGeofenceClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isDismissed) return

    val completedCount = (if (hasCheckedIn) 1 else 0) +
            (if (hasContacts) 1 else 0) +
            (if (hasGeofences) 1 else 0)

    val progress = completedCount / 3f
    val isAllCompleted = completedCount == 3

    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isAllCompleted) PrimaryEmerald.copy(alpha = 0.5f) else BorderColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con Icono, Título y Botón Cerrar
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
                            .size(28.dp)
                            .background(
                                if (isAllCompleted) PrimaryEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isAllCompleted) PrimaryEmerald else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAllCompleted) "¡Tu Escudo está Activo! 🛡️" else "Primeros Pasos de Seguridad",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllCompleted) PrimaryEmerald else TextPrimary
                        )
                        Text(
                            text = if (isAllCompleted) "Completaste las 3 misiones clave" else "$completedCount de 3 misiones completadas",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de progreso interactiva
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllCompleted) PrimaryEmerald else MaterialTheme.colorScheme.primary,
                trackColor = CardBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Misión 1: Check-in de bienestar
            ChecklistStepItem(
                stepNumber = "1",
                title = "Confirmá tu bienestar hoy",
                subtitle = "Tocá tu botón Estoy OK para iniciar el reloj",
                isCompleted = hasCheckedIn,
                actionLabel = "Confirmar",
                onClick = onCheckInClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Misión 2: Contacto de confianza
            ChecklistStepItem(
                stepNumber = "2",
                title = "Agregá a quién te cuidará",
                subtitle = "Recibirá un WhatsApp con tu mapa si no reportás",
                isCompleted = hasContacts,
                actionLabel = "Agregar",
                onClick = onAddContactClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Misión 3: Zona segura (Casa/Colegio)
            ChecklistStepItem(
                stepNumber = "3",
                title = "Creá tu primera Zona Segura",
                subtitle = "Marcá Casa o Trabajo para avisos de llegada",
                isCompleted = hasGeofences,
                actionLabel = "Crear",
                onClick = onCreateGeofenceClick
            )
        }
    }
}

@Composable
private fun ChecklistStepItem(
    stepNumber: String,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCompleted) PrimaryEmerald.copy(alpha = 0.08f) else CardBackground.copy(alpha = 0.6f))
            .clickable(enabled = !isCompleted, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isCompleted) PrimaryEmerald else TextMuted.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TextOnPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = stepNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) PrimaryEmerald else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!isCompleted) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
