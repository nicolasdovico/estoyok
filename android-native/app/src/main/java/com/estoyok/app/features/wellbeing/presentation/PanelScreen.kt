package com.estoyok.app.features.wellbeing.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*
import com.estoyok.app.core.util.rememberWindowInfo
import com.estoyok.app.features.wellbeing.data.model.CheckInDto
import java.text.SimpleDateFormat
import java.util.*

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.estoyok.app.services.MyFirebaseMessagingService

@Composable
fun PanelScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: PanelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Refrescar automáticamente y mantener activo el polling solo mientras la pantalla está visible
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.startActivePolling()
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    viewModel.stopActivePolling()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopActivePolling()
        }
    }

    // 2. Refrescar de inmediato ante notificaciones push de auto-checkin en vivo
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.refreshDashboard()
            }
        }
        val filter = IntentFilter(MyFirebaseMessagingService.ACTION_WELLBEING_UPDATED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.triggerSos(context)
            Toast.makeText(context, "¡SOS Silencioso Enviado!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Permiso de micrófono no concedido. Enviando SOS sin audio...", Toast.LENGTH_LONG).show()
            viewModel.triggerSos(context)
        }
    }

    val proactiveAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val hasAudio = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasAudio) {
            proactiveAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val handleSosAction: (android.content.Context) -> Unit = { ctx ->
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            viewModel.triggerSos(ctx)
            Toast.makeText(ctx, "¡SOS Silencioso Enviado!", Toast.LENGTH_LONG).show()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    PanelContent(
        userName = viewModel.user?.name ?: "Usuario",
        status = viewModel.status,
        checkInHistory = viewModel.checkInHistory,
        circleMembers = viewModel.circleMembers,
        activeCircleName = viewModel.activeCircleName,
        contactsCount = viewModel.totalAlertRecipients,
        emergencyContacts = viewModel.emergencyContacts,
        showManageContactsModal = viewModel.showManageContactsModal,
        intervalHours = viewModel.user?.checkinIntervalHours ?: 24,
        wifiAutoCheckinActive = viewModel.user?.isWifiAutoCheckinActive == true,
        isCheckingIn = viewModel.isCheckingIn,
        showSuccessDialog = viewModel.showCheckInSuccessDialog,
        isSosTriggered = viewModel.isSosTriggered,
        onRefresh = { viewModel.refreshDashboard() },
        onCheckIn = { viewModel.performCheckIn() },
        onDismissSuccessDialog = { viewModel.dismissSuccessDialog() },
        onOpenManageContactsModal = { viewModel.openManageContactsModal() },
        onDismissManageContactsModal = { viewModel.dismissManageContactsModal() },
        onAddContact = { name, phone, email, rel -> viewModel.addContact(name, phone, email, rel, context) },
        onUpdateContact = { id, name, phone, email, rel -> viewModel.updateContact(id, name, phone, email, rel, context) },
        onDeleteContact = { id -> viewModel.deleteContact(id) },
        onMoveContactUp = { idx -> viewModel.moveContactUp(idx) },
        onMoveContactDown = { idx -> viewModel.moveContactDown(idx) },
        onSos = handleSosAction,
        onSendReminder = { memberId -> viewModel.sendReminderPing(memberId, context) },
        onNavigateToSettings = onNavigateToSettings,
        showMandatoryDisclaimer = (viewModel.user != null && viewModel.user?.disclaimerAcceptedAt.isNullOrBlank()),
        onAcceptDisclaimer = { viewModel.acceptDisclaimer() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelContent(
    userName: String,
    status: WellbeingStatus,
    checkInHistory: List<CheckInDto>,
    circleMembers: List<com.estoyok.app.features.tracking.data.model.CircleMemberDto>,
    activeCircleName: String? = null,
    contactsCount: Int,
    emergencyContacts: List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>,
    showManageContactsModal: Boolean,
    intervalHours: Int,
    wifiAutoCheckinActive: Boolean,
    isCheckingIn: Boolean,
    showSuccessDialog: Boolean,
    isSosTriggered: Boolean,
    onRefresh: () -> Unit,
    onCheckIn: () -> Unit,
    onDismissSuccessDialog: () -> Unit,
    onOpenManageContactsModal: () -> Unit,
    onDismissManageContactsModal: () -> Unit,
    onAddContact: (String, String, String, String) -> Unit,
    onUpdateContact: (Int, String, String, String, String) -> Unit,
    onDeleteContact: (Int) -> Unit,
    onMoveContactUp: (Int) -> Unit,
    onMoveContactDown: (Int) -> Unit,
    onSos: (android.content.Context) -> Unit,
    onSendReminder: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    showMandatoryDisclaimer: Boolean = false,
    onAcceptDisclaimer: () -> Unit = {}
) {
    val context = LocalContext.current
    val windowInfo = rememberWindowInfo()

    val buttonSize = if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) 130.dp else 180.dp
    val spacingHeaderToBanner = if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) 12.dp else 20.dp
    val spacingBannerToButton = if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) 16.dp else 30.dp
    val spacingButtonToTitle = if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) 20.dp else 36.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showMandatoryDisclaimer) {
            DisclaimerMandatoryDialog(onAccept = onAcceptDisclaimer)
        }

        if (showSuccessDialog) {
            CheckInSuccessDialog(onDismiss = onDismissSuccessDialog)
        }

        if (showManageContactsModal) {
            ManageContactsModal(
                circleMembers = circleMembers,
                circleName = activeCircleName,
                emergencyContacts = emergencyContacts,
                onDismiss = onDismissManageContactsModal,
                onAddContact = onAddContact,
                onUpdateContact = onUpdateContact,
                onDeleteContact = onDeleteContact,
                onMoveContactUp = onMoveContactUp,
                onMoveContactDown = onMoveContactDown
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Header
                if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            IconButton(onClick = onNavigateToSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
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

                            Spacer(modifier = Modifier.width(8.dp))

                            // Silent SOS long-press trigger button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSosTriggered) PrimaryRed.copy(alpha = 0.5f) else PrimaryRed)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                onSos(context)
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
                } else {
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
                                color = MaterialTheme.colorScheme.primary
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

                            IconButton(onClick = onNavigateToSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacingHeaderToBanner))
                // 1. Wellbeing Status Banner
                StatusBanner(status = status)
                Spacer(modifier = Modifier.height(10.dp))
                // 1.5 Protection Summary Card
                ProtectionSummaryCard(
                    intervalHours = intervalHours,
                    contactsCount = contactsCount,
                    wifiAutoCheckinActive = wifiAutoCheckinActive,
                    onNavigateToSettings = onNavigateToSettings,
                    onManageContacts = onOpenManageContactsModal
                )
            }

            item {
                Spacer(modifier = Modifier.height(spacingBannerToButton))
                // 2. Check-In Main Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CheckInButton(
                        isCheckingIn = isCheckingIn,
                        onClick = onCheckIn,
                        size = buttonSize
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Tranquilidad del Núcleo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (circleMembers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text(
                            text = "Sin otros miembros en tu círculo. Invita a tu familia para ver su estado de bienestar aquí.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(circleMembers) { member ->
                    CircleMemberWellbeingCard(member = member, onSendReminder = { onSendReminder(member.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacingButtonToTitle))
                // 3. History Title
                Text(
                    text = "Historial de Reportes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // 4. History List
            if (checkInHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
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
                }
            } else {
                items(checkInHistory) { checkIn ->
                    CheckInItemRow(checkIn = checkIn)
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
            containerColor = PrimaryEmerald.copy(alpha = 0.12f)
            title = "Protegido y a Salvo"
            desc = "Debes reportarte antes de:\n${status.nextReportAt}"
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
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

            var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }

            if (status is WellbeingStatus.Safe) {
                androidx.compose.runtime.LaunchedEffect(status.nextReportTimestamp) {
                    while (true) {
                        currentTimeMs = System.currentTimeMillis()
                        kotlinx.coroutines.delay(1000L)
                    }
                }
            }

            val (formattedCountdown, progressVal, progressColor) = when (status) {
                is WellbeingStatus.Safe -> {
                    val remainingMs: Long = (status.nextReportTimestamp - currentTimeMs).coerceAtLeast(0L)
                    val totalMs: Long = status.totalDurationMs
                    val p: Float = if (totalMs > 0L) (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f) else 0f
                    val hours = remainingMs / 3600000L
                    val minutes = (remainingMs % 3600000L) / 60000L
                    val seconds = (remainingMs % 60000L) / 1000L
                    val formatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds)
                    val color = when {
                        p > 0.25f -> PrimaryEmerald
                        p > 0.10f -> PrimaryOrange
                        else -> PrimaryRed
                    }
                    Triple(formatted, p, color)
                }
                is WellbeingStatus.Expired -> Triple("00h 00m 00s (Expirado)", 0f, PrimaryRed)
                is WellbeingStatus.NoReports -> Triple("--h --m --s (Pendiente)", 0f, TextMuted)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱️ ", fontSize = 14.sp)
                            Text(
                                text = "Tiempo Restante",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = formattedCountdown,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = progressColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressVal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = BorderColor
                    )
                }
            }
        }
    }
}

@Composable
fun CheckInButton(
    isCheckingIn: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 180.dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
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
                    modifier = Modifier.size(size * 0.27f)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Estoy OK",
                        fontSize = if (size < 150.dp) 18.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (size >= 150.dp) {
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
}

@Composable
fun CheckInItemRow(checkIn: CheckInDto) {
    val windowInfo = rememberWindowInfo()

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

    if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sourceEmoji,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = userFriendlySource,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reporte procesado",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
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
}

@Composable
fun CircleMemberWellbeingCard(
    member: com.estoyok.app.features.tracking.data.model.CircleMemberDto,
    onSendReminder: () -> Unit
) {
    val windowInfo = rememberWindowInfo()

    val lastCheckInStr = member.lastCheckInAt
    val intervalHours = member.checkinIntervalHours ?: 24

    var isSafe = false
    var relativeTimeStr = "sin reporte"
    var expiryText: String? = null

    if (!lastCheckInStr.isNullOrBlank()) {
        val dateFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val checkInDate = sdf.parse(lastCheckInStr)
                if (checkInDate != null) {
                    val now = System.currentTimeMillis()
                    val intervalMs = intervalHours * 3600_000L
                    val expirationTimeMs = checkInDate.time + intervalMs

                    val diffMs = (now - checkInDate.time).coerceAtLeast(0L)
                    val diffMins = diffMs / 60000L
                    val diffHours = diffMins / 60L
                    val diffDays = diffHours / 24L

                    relativeTimeStr = when {
                        diffMins < 1 -> "hace 1m"
                        diffMins < 60 -> "hace ${diffMins}m"
                        diffHours < 24 -> "hace ${diffHours}h"
                        else -> "hace ${diffDays}d"
                    }

                    if (expirationTimeMs > now) {
                        isSafe = true

                        val expDate = Date(expirationTimeMs)
                        val nowCal = Calendar.getInstance()
                        val expCal = Calendar.getInstance().apply { time = expDate }

                        val isSameDay = nowCal.get(Calendar.YEAR) == expCal.get(Calendar.YEAR) &&
                                        nowCal.get(Calendar.DAY_OF_YEAR) == expCal.get(Calendar.DAY_OF_YEAR)

                        val tomCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                        val isTomorrow = tomCal.get(Calendar.YEAR) == expCal.get(Calendar.YEAR) &&
                                         tomCal.get(Calendar.DAY_OF_YEAR) == expCal.get(Calendar.DAY_OF_YEAR)

                        val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(expDate)

                        expiryText = when {
                            isSameDay -> "Vence hoy a las $timeFormatted hs"
                            isTomorrow -> "Vence mañana a las $timeFormatted hs"
                            else -> {
                                val dateFormatted = SimpleDateFormat("dd/MM 'a las' HH:mm", Locale.getDefault()).format(expDate)
                                "Vence el $dateFormatted hs"
                            }
                        }
                    }
                    break
                }
            } catch (_: Exception) {}
        }
    }

    val statusText = if (isSafe) {
        "🟢 Reportado OK ($relativeTimeStr)"
    } else {
        if (lastCheckInStr.isNullOrBlank()) "⚠️ Sin Reportes" else "⚠️ Reporte Vencido ($relativeTimeStr)"
    }

    val statusColor = if (isSafe) PrimaryEmerald else PrimaryOrange

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(2).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = member.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (expiryText != null) {
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = expiryText,
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Button(
                    onClick = onSendReminder,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSafe) PrimaryEmerald.copy(alpha = 0.15f) else PrimaryOrange.copy(alpha = 0.2f),
                        contentColor = if (isSafe) PrimaryEmerald else PrimaryOrange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Recordar 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(2).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = member.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (expiryText != null) {
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = expiryText,
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSendReminder,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSafe) PrimaryEmerald.copy(alpha = 0.15f) else PrimaryOrange.copy(alpha = 0.2f),
                        contentColor = if (isSafe) PrimaryEmerald else PrimaryOrange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Recordar 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProtectionSummaryCard(
    intervalHours: Int,
    contactsCount: Int,
    wifiAutoCheckinActive: Boolean,
    onNavigateToSettings: () -> Unit,
    onManageContacts: () -> Unit
) {
    val windowInfo = rememberWindowInfo()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️ ", fontSize = 14.sp)
                    Text(
                        text = "Configuración de Protección",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onNavigateToSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryEmerald.copy(alpha = 0.15f),
                        contentColor = PrimaryEmerald
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Configurar ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (windowInfo.isNarrowScreen || windowInfo.isHugeFont) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Intervalo", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${intervalHours}h Activo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Checkin", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (wifiAutoCheckinActive) "Wi-Fi Activo 📶" else "Manual 🟢",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f), thickness = 0.5.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onManageContacts() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contactos SOS", fontSize = 11.sp, color = TextMuted)
                        Surface(
                            color = PrimaryEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (contactsCount == 1) "1 Contacto ✏️" else "$contactsCount Contactos ✏️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEmerald,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Intervalo", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${intervalHours}h Activo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { onManageContacts() },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Contactos SOS", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            color = PrimaryEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (contactsCount == 1) "1 Contacto ✏️" else "$contactsCount Contactos ✏️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEmerald,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Checkin", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (wifiAutoCheckinActive) "Wi-Fi Activo 📶" else "Manual 🟢",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CheckInSuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryEmerald,
                    contentColor = TextOnPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Entendido 👍",
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🛡️✨", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Reporte Registrado!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = "Tu familia y contactos de emergencia han recibido tu confirmación de tranquilidad. Tu temporizador de bienestar ha sido reiniciado.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageContactsModal(
    circleMembers: List<com.estoyok.app.features.tracking.data.model.CircleMemberDto>,
    circleName: String? = null,
    emergencyContacts: List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>,
    onDismiss: () -> Unit,
    onAddContact: (String, String, String, String) -> Unit,
    onUpdateContact: (Int, String, String, String, String) -> Unit,
    onDeleteContact: (Int) -> Unit,
    onMoveContactUp: (Int) -> Unit,
    onMoveContactDown: (Int) -> Unit
) {
    var isFormVisible by remember { mutableStateOf(false) }
    var editingContactId by remember { mutableStateOf<Int?>(null) }
    var contactToDelete by remember { mutableStateOf<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto?>(null) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newRelationship by remember { mutableStateOf("") }

    // Validaciones en tiempo real para nombre, teléfono y email
    val cleanedPhone = newPhone.trim().replace(" ", "").replace("-", "")
    val isPhoneStartedWithPlus = cleanedPhone.startsWith("+")
    val withPlus = if (isPhoneStartedWithPlus) cleanedPhone else "+$cleanedPhone"
    val digitsOnly = withPlus.drop(1)
    val isDigitsValid = digitsOnly.all { it.isDigit() }

    val isArgPrefix = withPlus.startsWith("+54")
    val minPhoneDigits = if (isArgPrefix) 12 else 10
    val maxPhoneDigits = if (isArgPrefix) 13 else 15

    val nameError: String? = when {
        newName.isBlank() && (editingContactId != null || newName.isNotEmpty()) -> "El nombre completo es obligatorio"
        else -> null
    }

    val phoneError: String? = when {
        newPhone.isBlank() && editingContactId != null -> "El teléfono celular es obligatorio"
        newPhone.isBlank() -> null
        !isDigitsValid -> "Solo se permiten números y el '+' al inicio"
        digitsOnly.length < minPhoneDigits -> "Número incompleto (mínimo $minPhoneDigits dígitos con código de país y área)"
        digitsOnly.length > maxPhoneDigits -> "Número demasiado largo (máximo $maxPhoneDigits dígitos)"
        else -> null
    }

    val isEmailFormatValid = newEmail.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail.trim()).matches()
    val emailError: String? = when {
        newEmail.isBlank() -> null
        !isEmailFormatValid -> "Formato de correo electrónico inválido (ej: usuario@correo.com)"
        else -> null
    }

    val isNameValid = newName.trim().isNotBlank()
    val isPhoneValid = newPhone.trim().isNotBlank() && isDigitsValid && digitsOnly.length in minPhoneDigits..maxPhoneDigits
    val isEmailValid = emailError == null
    val isFormValid = isNameValid && isPhoneValid && isEmailValid

    // Interceptar botón atrás físico o por gestos del sistema
    BackHandler {
        if (isFormVisible) {
            isFormVisible = false
            editingContactId = null
            newName = ""
            newPhone = ""
            newEmail = ""
            newRelationship = ""
        } else {
            onDismiss()
        }
    }

    // Diálogo de confirmación para eliminar contacto
    contactToDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🗑️ ", fontSize = 20.sp)
                    Text("Eliminar Contacto SOS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar a \"${contact.name}\" de tus contactos de emergencia?",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        contact.id?.let { onDeleteContact(it) }
                        contactToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { contactToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", fontSize = 12.sp)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Dialog(
        onDismissRequest = {
            if (isFormVisible) {
                isFormVisible = false
                editingContactId = null
                newName = ""
                newPhone = ""
                newEmail = ""
                newRelationship = ""
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = DarkBackground,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            if (!isFormVisible) {
                // ==========================================
                // VISTA 1: LISTA DE CONTACTOS SOS
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header de la Vista 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CardBackground, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver al panel",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Contactos de Alerta SOS",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Avisos prioritarios por WhatsApp y Push",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sección: Miembros del Núcleo
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("🛡️ ", fontSize = 14.sp)
                                Text(
                                    text = if (!circleName.isNullOrBlank()) "Miembros de $circleName (Automáticos)" else "Miembros del Núcleo (Automáticos)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEmerald
                                )
                            }
                        }

                        if (circleMembers.isEmpty()) {
                            item {
                                Surface(
                                    color = CardBackground.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Sin otros miembros en el núcleo.",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            items(circleMembers) { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardBackground.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(member.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(member.email, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Surface(
                                        color = PrimaryEmerald.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "🛡️ Núcleo",
                                            fontSize = 11.sp,
                                            color = PrimaryEmerald,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sección: Contactos Externos
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("📞 ", fontSize = 14.sp)
                                Text(
                                    text = "Contactos Externos (WhatsApp)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Se alertarán secuencialmente en este orden de prioridad:",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        if (emergencyContacts.isEmpty()) {
                            item {
                                Surface(
                                    color = CardBackground.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No has registrado contactos externos aún.\nPulsa el botón de abajo para agregar uno.",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(emergencyContacts) { index, contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardBackground, RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            color = PrimaryEmerald.copy(alpha = 0.15f),
                                            contentColor = PrimaryEmerald,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(contact.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            val contactSubtext = if (!contact.email.isNullOrBlank()) "${contact.phone} • ${contact.email}" else contact.phone
                                            Text("$contactSubtext • ${contact.relationship ?: "Familiar"}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onMoveContactUp(index) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Subir prioridad",
                                                tint = if (index > 0) PrimaryEmerald else TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onMoveContactDown(index) },
                                            enabled = index < emergencyContacts.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Bajar prioridad",
                                                tint = if (index < emergencyContacts.size - 1) PrimaryEmerald else TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                editingContactId = contact.id
                                                newName = contact.name
                                                newPhone = contact.phone
                                                newEmail = contact.email ?: ""
                                                newRelationship = contact.relationship ?: ""
                                                isFormVisible = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar contacto",
                                                tint = PrimaryOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        contact.id?.let {
                                            IconButton(
                                                onClick = { contactToDelete = contact },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar contacto",
                                                    tint = PrimaryRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Botón inferior para agregar nuevo contacto
                    Button(
                        onClick = {
                            editingContactId = null
                            newName = ""
                            newPhone = ""
                            newEmail = ""
                            newRelationship = ""
                            isFormVisible = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEmerald,
                            contentColor = TextOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = TextOnPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agregar Nuevo Contacto Externo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextOnPrimary
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // VISTA 2: FORMULARIO DE AGREGAR / EDITAR
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header de la Vista 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isFormVisible = false
                                editingContactId = null
                                newName = ""
                                newPhone = ""
                                newEmail = ""
                                newRelationship = ""
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(CardBackground, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver a la lista",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingContactId != null) "✏️ Editar Contacto SOS" else "➕ Nuevo Contacto SOS",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (editingContactId != null) "Modifica los datos del contacto de emergencia" else "Ingresa los datos del nuevo contacto externo",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = CardBackground,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    label = { Text("Nombre Completo", fontSize = 12.sp) },
                                    placeholder = { Text("Ej. María García", fontSize = 12.sp, color = TextMuted) },
                                    supportingText = {
                                        if (nameError != null) {
                                            Text(nameError, fontSize = 11.sp, color = PrimaryRed)
                                        }
                                    },
                                    isError = nameError != null,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        capitalization = KeyboardCapitalization.Words
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryEmerald,
                                        unfocusedBorderColor = BorderColor,
                                        focusedLabelColor = PrimaryEmerald,
                                        errorBorderColor = PrimaryRed,
                                        errorLabelColor = PrimaryRed
                                    )
                                )

                                OutlinedTextField(
                                    value = newPhone,
                                    onValueChange = { newPhone = it },
                                    label = { Text("Teléfono Celular con prefijo", fontSize = 12.sp) },
                                    placeholder = { Text("Ej. +5491123456789", fontSize = 12.sp, color = TextMuted) },
                                    supportingText = {
                                        if (phoneError != null) {
                                            Text(phoneError, fontSize = 11.sp, color = PrimaryRed)
                                        } else {
                                            Text("Incluye el código de país (Ej: +549...)", fontSize = 10.sp, color = TextMuted)
                                        }
                                    },
                                    isError = phoneError != null,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryEmerald,
                                        unfocusedBorderColor = BorderColor,
                                        focusedLabelColor = PrimaryEmerald,
                                        errorBorderColor = PrimaryRed,
                                        errorLabelColor = PrimaryRed
                                    )
                                )

                                OutlinedTextField(
                                    value = newEmail,
                                    onValueChange = { newEmail = it },
                                    label = { Text("Email / Correo (Opcional)", fontSize = 12.sp) },
                                    placeholder = { Text("Ej. contacto@gmail.com", fontSize = 12.sp, color = TextMuted) },
                                    supportingText = {
                                        if (emailError != null) {
                                            Text(emailError, fontSize = 11.sp, color = PrimaryRed)
                                        } else {
                                            Text("Opcional: Para alertas y respaldos por email", fontSize = 10.sp, color = TextMuted)
                                        }
                                    },
                                    isError = emailError != null,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryEmerald,
                                        unfocusedBorderColor = BorderColor,
                                        focusedLabelColor = PrimaryEmerald,
                                        errorBorderColor = PrimaryRed,
                                        errorLabelColor = PrimaryRed
                                    )
                                )

                                OutlinedTextField(
                                    value = newRelationship,
                                    onValueChange = { newRelationship = it },
                                    label = { Text("Parentesco (Opcional)", fontSize = 12.sp) },
                                    placeholder = { Text("Ej. Hermana, Madre, Amigo", fontSize = 12.sp, color = TextMuted) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        capitalization = KeyboardCapitalization.Words
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryEmerald,
                                        unfocusedBorderColor = BorderColor,
                                        focusedLabelColor = PrimaryEmerald
                                    )
                                )
                            }
                        }
                    }

                    // Botones de acción inferiores
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isFormValid) {
                                    val formattedPhone = if (cleanedPhone.startsWith("+")) cleanedPhone else "+$cleanedPhone"
                                    val currentId = editingContactId
                                    if (currentId != null) {
                                        onUpdateContact(currentId, newName.trim(), formattedPhone, newEmail.trim(), newRelationship.trim())
                                    } else {
                                        onAddContact(newName.trim(), formattedPhone, newEmail.trim(), newRelationship.trim())
                                    }
                                    editingContactId = null
                                    newName = ""
                                    newPhone = ""
                                    newEmail = ""
                                    newRelationship = ""
                                    isFormVisible = false
                                }
                            },
                            enabled = isFormValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryEmerald,
                                contentColor = TextOnPrimary,
                                disabledContainerColor = CardBackground,
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = if (editingContactId != null) "Actualizar Contacto ✏️" else "Guardar Contacto 💾",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isFormValid) TextOnPrimary else TextMuted
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                editingContactId = null
                                newName = ""
                                newPhone = ""
                                newEmail = ""
                                newRelationship = ""
                                isFormVisible = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Cancelar", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PanelScreenPreview() {
    EstoyOkTheme {
        PanelContent(
            userName = "Juan Pérez",
            status = WellbeingStatus.Safe(
                nextReportAt = "07/07/2026 23:59:59",
                nextReportTimestamp = System.currentTimeMillis() + 86400000L,
                totalDurationMs = 86400000L
            ),
            checkInHistory = listOf(
                CheckInDto(1, "manual", "2026-07-06T20:30:00Z"),
                CheckInDto(2, "wifi", "2026-07-06T15:20:00Z"),
                CheckInDto(3, "movement", "2026-07-06T10:15:00Z")
            ),
            circleMembers = listOf(
                com.estoyok.app.features.tracking.data.model.CircleMemberDto(
                    id = 2,
                    name = "Analía",
                    email = "analia@gmail.com",
                    phone = "+5491100001111",
                    avatarUrl = null,
                    currentLocation = com.estoyok.app.features.tracking.data.model.MemberLocationDto(
                        latitude = -34.6,
                        longitude = -58.4,
                        accuracy = 10f,
                        batteryLevel = 85f,
                        isBatteryLow = false,
                        isTrackingActive = true,
                        gpsEnabled = true,
                        recordedAt = "2026-07-22T10:30:00Z",
                        speed = 0f,
                        isDriving = false,
                        isOffline = false,
                        lastSeenAt = "2026-07-22T10:30:00Z"
                    )
                ),
                com.estoyok.app.features.tracking.data.model.CircleMemberDto(
                    id = 3,
                    name = "Nicolás",
                    email = "nicolas@gmail.com",
                    phone = "+5491100002222",
                    avatarUrl = null,
                    currentLocation = null
                )
            ),
            contactsCount = 2,
            emergencyContacts = emptyList(),
            showManageContactsModal = false,
            intervalHours = 24,
            wifiAutoCheckinActive = true,
            isCheckingIn = false,
            showSuccessDialog = false,
            isSosTriggered = false,
            onRefresh = {},
            onCheckIn = {},
            onDismissSuccessDialog = {},
            onOpenManageContactsModal = {},
            onDismissManageContactsModal = {},
            onAddContact = { _, _, _, _ -> },
            onUpdateContact = { _, _, _, _, _ -> },
            onDeleteContact = {},
            onMoveContactUp = {},
            onMoveContactDown = {},
            onSos = {},
            onSendReminder = {},
            onNavigateToSettings = {}
        )
    }
}

@Composable
fun DisclaimerMandatoryDialog(
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {}, // Modal bloqueante, no se cierra sin aceptar
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Condiciones y Protección",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Bienvenido a Estoy Ok. Para utilizar la plataforma de protección familiar, debes declarar conocer las condiciones operativas y de privacidad:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                Text(
                    text = "1. Conectividad e Internet\nRequiere datos móviles (4G/5G) o Wi-Fi activo para transmitir tu ubicación y enviar alertas de emergencia.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                Text(
                    text = "2. Permisos y Batería\nRequiere ubicación 'Permitir todo el tiempo' y exención de ahorro de energía para garantizar el rastreo en segundo plano.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                Text(
                    text = "3. Complemento de Emergencia\nEstoy Ok es una red privada familiar y NO sustituye al 911, 107 ni a las fuerzas de seguridad públicas.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                Text(
                    text = "4. Check-in e Inactividad\nLas alertas se disparan al expirar el tiempo configurado. En Modo Sueño, las notificaciones se pausarán.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                Text(
                    text = "5. Privacidad Consensuada\nTu posición solo se comparte con los miembros del núcleo activo que hayas seleccionado voluntariamente.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                Text(
                    text = "6. Sin Monitoreo ni Venta de Datos\nLos administradores de Estoy Ok NO rastrean ni vigilan a los usuarios. Tu ubicación NUNCA será vendida ni cedida a terceros.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryEmerald,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = TextOnPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Entendido y Aceptar Condiciones 🟢", fontWeight = FontWeight.Bold, color = TextOnPrimary, fontSize = 13.sp)
            }
        }
    )
}
