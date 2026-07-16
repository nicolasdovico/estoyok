package com.estoyok.app.features.wellbeing.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*
import com.estoyok.app.features.auth.presentation.AuthViewModel
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Permiso de segundo plano concedido. La app te protegerá en todo momento.", Toast.LENGTH_LONG).show()
            if (!viewModel.isTrackingServiceRunning) {
                viewModel.toggleTrackingService(context)
            }
        } else {
            Toast.makeText(context, "El rastreo en segundo plano requiere el permiso 'Permitir todo el tiempo'", Toast.LENGTH_LONG).show()
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true
        }
        
        if (fineLocationGranted && notificationGranted) {
            val hasBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            
            if (!hasBackground) {
                showBackgroundLocationDialog = true
            } else {
                viewModel.toggleTrackingService(context)
            }
        } else {
            Toast.makeText(context, "Se necesitan permisos de ubicación y notificaciones para el rastreo", Toast.LENGTH_LONG).show()
        }
    }
    
    // Clear messages when user leaves screen or on start
    LaunchedEffect(key1 = true) {
        viewModel.clearMessages()
        viewModel.refreshServiceStatus()
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
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Title
            Text(
                text = "Configuración de Seguridad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Feedback messages (Snackbar Mock)
            viewModel.messageSuccess?.let { success ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryEmerald.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "✓ $success",
                        color = PrimaryEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            viewModel.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "✗ $error",
                        color = PrimaryRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. User Profile Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = viewModel.userProfile?.name ?: "Usuario",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = viewModel.userProfile?.email ?: "",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        if (viewModel.userProfile?.isPremium == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = PrimaryOrange.copy(alpha = 0.2f),
                                contentColor = PrimaryOrange,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "⭐️ Premium PRO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Check-In Interval Settings
            SettingsCard(title = "Intervalo de Reporte") {
                Column {
                    Text(
                        text = "Elige cada cuántas horas debes reportarte antes de disparar alertas a tus contactos de emergencia.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(12, 24, 48).forEach { hours ->
                            FilterChip(
                                selected = viewModel.checkinIntervalHours == hours,
                                onClick = { viewModel.saveCheckinInterval(hours) },
                                label = { Text("$hours Horas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // 3. Sleep Mode (Modo Sueño) Configuration
            SettingsCard(title = "Modo Sueño (Horas Silenciosas)") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Durante estas horas, las alertas y recordatorios de inactividad se pausarán automáticamente para no interrumpir tu sueño.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Habilitar Modo Sueño",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Switch(
                            checked = viewModel.quietHoursEnabled,
                            onCheckedChange = {
                                viewModel.saveQuietHoursSettings(it, viewModel.quietHoursStart, viewModel.quietHoursEnd)
                            }
                        )
                    }

                    if (viewModel.quietHoursEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.quietHoursStart,
                                onValueChange = { viewModel.quietHoursStart = it },
                                label = { Text("Hora Inicio") },
                                placeholder = { Text("22:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = viewModel.quietHoursEnd,
                                onValueChange = { viewModel.quietHoursEnd = it },
                                label = { Text("Hora Fin") },
                                placeholder = { Text("08:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.saveQuietHoursSettings(
                                    true,
                                    viewModel.quietHoursStart.trim(),
                                    viewModel.quietHoursEnd.trim()
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Guardar Horas", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. SMS / WhatsApp (Twilio Webhook Toggle)
            SettingsCard(title = "Método de Reporte Twilio") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reporte por SMS / WhatsApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Habilita responder a las notificaciones de emergencia directamente con un mensaje de texto para confirmar que estás a salvo.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = viewModel.allowSmsWhatsappCheckin,
                        onCheckedChange = { viewModel.toggleSmsWhatsapp(it) }
                    )
                }
            }

            // 5. Passive Auto-Check-in
            SettingsCard(title = "Auto-Check-in Pasivo") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Automatiza tus reportes cotidianos sin necesidad de abrir la aplicación constantemente.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    // WiFi Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-reportarse por Wi-Fi Seguro",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Se reporta al conectarse al Wi-Fi de tu casa o trabajo.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = viewModel.wifiCheckinEnabled,
                            onCheckedChange = {
                                viewModel.saveAutomationSettings(it, viewModel.safeWifiSsid, viewModel.sensorCheckinEnabled)
                            }
                        )
                    }

                    if (viewModel.wifiCheckinEnabled) {
                        OutlinedTextField(
                            value = viewModel.safeWifiSsid,
                            onValueChange = { viewModel.safeWifiSsid = it },
                            label = { Text("SSID de Wi-Fi de Confianza") },
                            placeholder = { Text("Ej. MiCasaWiFi_5G") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                    // Sensor Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-reportarse por Actividad Física",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Se reporta al detectar más de 100 pasos en 1 hora.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = viewModel.sensorCheckinEnabled,
                            onCheckedChange = {
                                viewModel.saveAutomationSettings(viewModel.wifiCheckinEnabled, viewModel.safeWifiSsid, it)
                            }
                        )
                    }
                }
            }

            // 6. Real-Time Tracking Setting (Rastreo)
            SettingsCard(title = "Ubicación en Tiempo Real") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compartir Ubicación (Rastreo)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Permite a tu grupo familiar ver tu posición actual en vivo sobre el mapa.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = viewModel.isTrackingServiceRunning,
                        onCheckedChange = { checked ->
                            val hasLocation = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }

                            if (checked) {
                                if (hasLocation && hasNotifications) {
                                    val hasBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    } else {
                                        true
                                    }
                                    if (!hasBackground) {
                                        showBackgroundLocationDialog = true
                                    } else if (!viewModel.isTrackingServiceRunning) {
                                        viewModel.toggleTrackingService(context)
                                    }
                                } else {
                                    val reqs = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        reqs.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    permissionsLauncher.launch(reqs.toTypedArray())
                                }
                            } else {
                                if (viewModel.isTrackingServiceRunning) {
                                    viewModel.toggleTrackingService(context)
                                }
                            }
                        }
                    )
                }
            }

            // 7. Emergency Contacts
            SettingsCard(title = "Contactos de Emergencia") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Contactos que serán alertados si no realizas tu check-in a tiempo. Organízalos con las flechas para fijar su orden de llamada secuencial.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    // Contacts List
                    if (viewModel.contacts.isEmpty()) {
                        Text(
                            text = "No has agregado contactos de emergencia aún.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        viewModel.contacts.forEachIndexed { index, contact ->
                            ContactRow(
                                contact = contact,
                                index = index,
                                totalSize = viewModel.contacts.size,
                                onMoveUp = { viewModel.moveContactUp(index) },
                                onMoveDown = { viewModel.moveContactDown(index) },
                                onDelete = { contact.id?.let { viewModel.deleteContact(it) } }
                            )
                            if (index < viewModel.contacts.size - 1) {
                                HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // Add Contact Form
                    Text(
                        text = "Agregar Nuevo Contacto",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = viewModel.newContactName,
                        onValueChange = { viewModel.newContactName = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.newContactPhone,
                        onValueChange = { viewModel.newContactPhone = it },
                        label = { Text("Teléfono") },
                        placeholder = { Text("+54911...") },
                        supportingText = { Text("Debe iniciar con '+' prefijo país") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.newContactEmail,
                        onValueChange = { viewModel.newContactEmail = it },
                        label = { Text("Correo (Opcional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.newContactRelationship,
                        onValueChange = { viewModel.newContactRelationship = it },
                        label = { Text("Parentesco / Relación") },
                        placeholder = { Text("Ej. Madre, Esposo, Amigo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { viewModel.addContact() },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Agregar Contacto", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 7. Logout Button
            Button(
                onClick = { authViewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Cerrar Sesión",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showBackgroundLocationDialog) {
            AlertDialog(
                onDismissRequest = { showBackgroundLocationDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rastreo en Segundo Plano",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Estoy Ok recopila datos de ubicación para permitir el rastreo en tiempo real, alertas de zonas seguras y detección de choques incluso cuando la app está cerrada o no está en uso.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Para activar esta protección continua, selecciona 'Permitir todo el tiempo' en la configuración de ubicación.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showBackgroundLocationDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        }
                    ) {
                        Text("Configurar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackgroundLocationDialog = false }) {
                        Text("Ahora no", color = MaterialTheme.colorScheme.outline)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
fun ContactRow(
    contact: EmergencyContactDto,
    index: Int,
    totalSize: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Tag Badge
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "#${index + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = contact.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${contact.phone} • ${contact.relationship ?: "Familiar"}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Reordering Arrows & Delete Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onMoveUp,
                enabled = index > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move Up",
                    tint = if (index > 0) MaterialTheme.colorScheme.primary else TextMuted
                )
            }

            IconButton(
                onClick = onMoveDown,
                enabled = index < totalSize - 1
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move Down",
                    tint = if (index < totalSize - 1) MaterialTheme.colorScheme.primary else TextMuted
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Contact",
                    tint = PrimaryRed
                )
            }
        }
    }
}
