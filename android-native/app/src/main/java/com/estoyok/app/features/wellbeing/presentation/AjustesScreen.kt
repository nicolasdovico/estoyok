package com.estoyok.app.features.wellbeing.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*
import androidx.navigation.NavHostController
import com.estoyok.app.core.navigation.Screen
import com.estoyok.app.features.auth.presentation.AuthViewModel
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavHostController? = null,
    viewModel: AjustesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

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

    val wifiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            val detectedSsid = detectConnectedWifiSsid(context)
            if (!detectedSsid.isNullOrBlank()) {
                viewModel.safeWifiSsid = detectedSsid
                viewModel.saveAutomationSettings(
                    true,
                    detectedSsid,
                    viewModel.sensorCheckinEnabled
                )
                Toast.makeText(context, "Wi-Fi detectado y guardado: $detectedSsid", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Asegúrate de estar conectado a Wi-Fi y tener la ubicación GPS activada.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Se requieren permisos de ubicación para leer el nombre de la red Wi-Fi.", Toast.LENGTH_LONG).show()
        }
    }
    
    // Display feedback messages via Toast
    LaunchedEffect(viewModel.messageSuccess, viewModel.errorMessage) {
        viewModel.messageSuccess?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        viewModel.errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(key1 = true) {
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
                color = MaterialTheme.colorScheme.primary
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
                                color = PrimaryEmerald.copy(alpha = 0.2f),
                                contentColor = PrimaryEmerald,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "👑 Socio Premium PRO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subscription & Free Trial Promo Card
            SettingsCard(title = if (viewModel.userProfile?.isPremium == true) "Suscripción Premium 👑" else "Estado de tu Plan 🛡️") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (viewModel.userProfile?.isPremium == true) {
                        Text(
                            text = "Tu cuenta cuenta con protección completa PRO activa: WhatsApp, SOS ambiental, telemetría vehicular e historial por 30 días.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "Actualmente estás en el Plan Gratuito (Limitado). Obtén Zonas Seguras ilimitadas, alertas por WhatsApp y SOS con grabación de audio.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { navController?.navigate(Screen.Premium.route) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryEmerald,
                                contentColor = TextOnPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Iniciar Prueba Gratis (7 Días)", fontWeight = FontWeight.Bold, color = TextOnPrimary)
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
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = viewModel.quietHoursEnabled,
                            onCheckedChange = {
                                viewModel.saveQuietHoursSettings(it, viewModel.quietHoursStart, viewModel.quietHoursEnd)
                            },
                            modifier = Modifier.scale(0.75f)
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
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = TextOnPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Guardar Horas", fontWeight = FontWeight.Bold, color = TextOnPrimary)
                        }
                    }
                }
            }

            // 4. WhatsApp (UltraMsg Webhook Toggle)
            SettingsCard(title = "Reporte por WhatsApp") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reporte por WhatsApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Habilita responder a las notificaciones de emergencia directamente con un mensaje de WhatsApp para confirmar que estás a salvo.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = viewModel.allowSmsWhatsappCheckin,
                        onCheckedChange = { viewModel.toggleSmsWhatsapp(it) },
                        modifier = Modifier.scale(0.75f)
                    )
                }
            }

            // 5. Alertas de Proximidad y Zonas
            SettingsCard(title = "Alertas de Proximidad y Zonas") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notificaciones de Entrada/Salida",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recibe avisos automáticos cuando los miembros de tus grupos entren o salgan de las Zonas Seguras configuradas.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = viewModel.proximityAlertsEnabled,
                        onCheckedChange = { viewModel.toggleProximityAlerts(it) },
                        modifier = Modifier.scale(0.75f)
                    )
                }
            }

            // 6. Respuestas y Coordinación de Emergencia
            SettingsCard(title = "Respuestas y Coordinación de Rescate 🛡️") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compartir Respuestas de Apoyo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notifica por WhatsApp y Push a tus contactos y a ti cuando alguien responde 'Voy en camino' o 'Enterado' en tu mapa de alerta.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = viewModel.shareContactResponses,
                        onCheckedChange = { viewModel.toggleShareContactResponses(it) },
                        modifier = Modifier.scale(0.75f)
                    )
                }
            }

            // 6. Passive Auto-Check-in
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
                            },
                            modifier = Modifier.scale(0.75f)
                        )
                    }

                    if (viewModel.wifiCheckinEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = viewModel.safeWifiSsid,
                                    onValueChange = { viewModel.safeWifiSsid = it },
                                    label = { Text("SSID de Wi-Fi de Confianza") },
                                    placeholder = { Text("Ej. MiCasaWiFi_5G") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = {
                                        viewModel.saveAutomationSettings(
                                            true,
                                            viewModel.safeWifiSsid.trim(),
                                            viewModel.sensorCheckinEnabled
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = TextOnPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Guardar", fontWeight = FontWeight.Bold, color = TextOnPrimary, fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                                    val permissionsToRequest = mutableListOf<String>()
                                    if (!hasFineLocation) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                                    if (!hasCoarseLocation) permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                                            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                                        }
                                    }

                                    if (permissionsToRequest.isNotEmpty()) {
                                        wifiPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                                    } else {
                                        val detectedSsid = detectConnectedWifiSsid(context)
                                        if (!detectedSsid.isNullOrBlank()) {
                                            viewModel.safeWifiSsid = detectedSsid
                                            viewModel.saveAutomationSettings(
                                                true,
                                                detectedSsid,
                                                viewModel.sensorCheckinEnabled
                                            )
                                            Toast.makeText(context, "Wi-Fi detectado y guardado: $detectedSsid", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No se pudo leer la Wi-Fi. Asegúrate de estar conectado a Wi-Fi y tener el GPS activado.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text("📶 Usar Wi-Fi Actual", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
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
                            },
                            modifier = Modifier.scale(0.75f)
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
                        },
                        modifier = Modifier.scale(0.75f)
                    )
                }
            }



            // 7. Disclaimer & Operational Terms Card
            SettingsCard(title = "Condiciones del Servicio ℹ️") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Estoy Ok requiere Datos Móviles / Wi-Fi y ubicación continua ('Permitir todo el tiempo') para enviar alertas de inactividad, SOS y Zonas Seguras. No reemplaza a las líneas públicas de emergencia 911.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                    OutlinedButton(
                        onClick = { showDisclaimerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Condiciones del Servicio 📋", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 8. Logout Button
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

        if (showDisclaimerDialog) {
            AlertDialog(
                onDismissRequest = { showDisclaimerDialog = false },
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
                            text = "Condiciones de Servicio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. Conectividad e Internet\nEstoy Ok requiere datos móviles (4G/5G) o Wi-Fi activo. Si el teléfono pierde conexión, las alertas y la ubicación en tiempo real se pausarán hasta recuperar la señal.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "2. Permisos y Batería\nRequiere permisos de ubicación 'Permitir todo el tiempo' y exención de ahorro de batería para evitar que el sistema operativo suspenda el rastreo y el check-in pasivo.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "3. Complemento de Emergencia\nEstoy Ok NO reemplaza al 911, 107 ni a las fuerzas de seguridad públicas. Es una red privada de protección entre familiares.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "4. Check-in e Inactividad\nLas alertas se disparan al vencer el intervalo configurado (ej. 24h). En Modo Sueño, las notificaciones se pausarán automáticamente.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "5. Privacidad Consensuada\nTu ubicación solo es visible para los integrantes autorizados del núcleo activo que hayas seleccionado voluntariamente.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "6. Privacidad y No Monitoreo Administrativo\nLos administradores de Estoy Ok NO monitorean ni vigilan a los usuarios. Tu ubicación e historial NUNCA serán vendidos ni cedidos a terceros o anunciantes.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDisclaimerDialog = false }
                    ) {
                        Text("Entendido y Aceptado", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

fun detectConnectedWifiSsid(context: Context): String? {
    return try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null && !wifiInfo.ssid.isNullOrBlank()) {
            var ssid = wifiInfo.ssid
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length - 1)
            }
            if (ssid != "<unknown ssid>" && ssid.isNotBlank()) {
                return ssid
            }
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val transportInfo = capabilities.transportInfo
            if (transportInfo is android.net.wifi.WifiInfo) {
                var ssid = transportInfo.ssid
                if (!ssid.isNullOrBlank()) {
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length - 1)
                    }
                    if (ssid != "<unknown ssid>" && ssid.isNotBlank()) {
                        return ssid
                    }
                }
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}


