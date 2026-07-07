package com.estoyok.app.features.wellbeing.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*
import com.estoyok.app.features.wellbeing.data.model.CheckInDto
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PanelScreen(
    viewModel: PanelViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    PanelContent(
        userName = viewModel.user?.name ?: "Usuario",
        status = viewModel.status,
        checkInHistory = viewModel.checkInHistory,
        isCheckingIn = viewModel.isCheckingIn,
        isSosTriggered = viewModel.isSosTriggered,
        onRefresh = { viewModel.refreshDashboard() },
        onCheckIn = { viewModel.performCheckIn() },
        onSos = { ctx -> viewModel.triggerSos(ctx) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelContent(
    userName: String,
    status: WellbeingStatus,
    checkInHistory: List<CheckInDto>,
    isCheckingIn: Boolean,
    isSosTriggered: Boolean,
    onRefresh: () -> Unit,
    onCheckIn: () -> Unit,
    onSos: (android.content.Context) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mi Bienestar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Actualizar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Silent SOS long-press trigger button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSosTriggered) PrimaryRed.copy(alpha = 0.5f) else PrimaryRed)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        onSos(context)
                                        Toast.makeText(context, "¡SOS Silencioso Enviado!", Toast.LENGTH_LONG).show()
                                    },
                                    onTap = {
                                        Toast.makeText(context, "Mantén presionado por 3 segundos para activar SOS", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isSosTriggered) "SOS... 🚨" else "SOS",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Wellbeing Status Banner
            StatusBanner(status = status)

            Spacer(modifier = Modifier.height(30.dp))

            // 2. Check-In Main Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                CheckInButton(
                    isCheckingIn = isCheckingIn,
                    onClick = onCheckIn
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 3. History Title
            Text(
                text = "Historial de Reportes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 4. History List
            if (checkInHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no tienes reportes guardados.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(checkInHistory) { checkIn ->
                        CheckInItemRow(checkIn = checkIn)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBanner(status: WellbeingStatus) {
    val containerColor: Color
    val title: String
    val desc: String
    val emoji: String

    when (status) {
        is WellbeingStatus.NoReports -> {
            containerColor = BorderColor
            title = "Sin Reportes"
            desc = "Aún no has enviado tu primer reporte. Presiona el botón \"Estoy OK\" para iniciar tu protección diaria."
            emoji = "ℹ️"
        }
        is WellbeingStatus.Safe -> {
            containerColor = PrimaryEmerald.copy(alpha = 0.15f)
            title = "Protegido y a Salvo"
            desc = "Tu temporizador está activo. Debes reportarte antes de:\n${status.nextReportAt}"
            emoji = "🛡️"
        }
        is WellbeingStatus.Expired -> {
            containerColor = PrimaryRed.copy(alpha = 0.15f)
            title = "Reporte Vencido"
            desc = "El tiempo límite expiró. Presiona el botón \"Estoy OK\" de inmediato para evitar falsas alertas a tus contactos."
            emoji = "⚠️"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (status) {
                is WellbeingStatus.NoReports -> BorderColor.copy(alpha = 0.5f)
                is WellbeingStatus.Safe -> PrimaryEmerald.copy(alpha = 0.5f)
                is WellbeingStatus.Expired -> PrimaryRed.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun CheckInButton(
    isCheckingIn: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(180.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryEmerald, Color(0xFF0F6D38))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCheckingIn) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Estoy OK",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reportar bienestar",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckInItemRow(checkIn: CheckInDto) {
    val userFriendlySource = when (checkIn.source) {
        "manual" -> "Vía Manual"
        "wifi" -> "Vía Wi-Fi Seguro"
        "movement" -> "Vía Sensor"
        "sms" -> "Vía SMS"
        "whatsapp" -> "Vía WhatsApp"
        else -> "Vía Reporte"
    }

    val sourceEmoji = when (checkIn.source) {
        "manual" -> "👆"
        "wifi" -> "📶"
        "movement" -> "🚶"
        "sms" -> "💬"
        "whatsapp" -> "🟢"
        else -> "📝"
    }

    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val inputFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = try {
            inputFormat.parse(checkIn.createdAt)
        } catch (e: Exception) {
            inputFallback.parse(checkIn.createdAt)
        }
        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        checkIn.createdAt
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = sourceEmoji,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Column {
                Text(
                    text = userFriendlySource,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Reporte procesado",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Text(
            text = formattedDate,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PanelScreenPreview() {
    EstoyOkTheme {
        PanelContent(
            userName = "Juan Pérez",
            status = WellbeingStatus.Safe("07/07/2026 23:59:59"),
            checkInHistory = listOf(
                CheckInDto(1, "manual", "2026-07-06T20:30:00Z"),
                CheckInDto(2, "wifi", "2026-07-06T15:20:00Z"),
                CheckInDto(3, "movement", "2026-07-06T10:15:00Z")
            ),
            isCheckingIn = false,
            isSosTriggered = false,
            onRefresh = {},
            onCheckIn = {},
            onSos = {}
        )
    }
}
