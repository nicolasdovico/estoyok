package com.estoyok.app.features.tracking.presentation

import android.Manifest
import android.widget.Toast
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.draw.scale
import androidx.navigation.NavHostController
import com.estoyok.app.core.navigation.Screen
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
import com.estoyok.app.features.tracking.data.model.CircleMemberDto
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    navController: NavHostController? = null,
    viewModel: MapaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
            viewModel.toggleTrackingService(context)
        } else {
            Toast.makeText(context, "Se necesitan permisos de ubicación y notificaciones para el rastreo", Toast.LENGTH_LONG).show()
        }
    }
    var isCircleDropdownExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    // LatLng for Argentina/Buenos Aires default center
    val defaultCenter = LatLng(-34.6037, -58.3816)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 11f)
    }

    // Reactively center map on selected member if requested
    var selectedMemberForMap by remember { mutableStateOf<CircleMemberDto?>(null) }
    LaunchedEffect(selectedMemberForMap) {
        selectedMemberForMap?.currentLocation?.let { loc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(loc.latitude, loc.longitude),
                15f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Google Map Layer
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = viewModel.isServiceRunning,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            )
        ) {
            // Render markers for all nucleus members with valid location coordinates
            viewModel.selectedCircleMembers.forEach { member ->
                val loc = member.currentLocation
                if (loc != null) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    val titleText = member.name
                    val snippetText = buildString {
                        append("Batería: ${loc.batteryLevel?.let { (it * 100).toInt() } ?: 100}%")
                        if (loc.isDriving == true) {
                            append(" • Conduciendo: ${loc.speed?.toInt() ?: 0} km/h")
                        }
                    }

                    Marker(
                        state = rememberMarkerState(position = latLng),
                        title = titleText,
                        snippet = snippetText,
                        onClick = {
                            selectedMemberForMap = member
                            false // Return false to show standard info window popup
                        }
                    )
                }
            }
        }

        // 2. Floating Header: Circle Selector & Tracking Switch
        // 2a. Floating Header: Circle Selector Combo (Centered Pill)
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .padding(top = 16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable { isCircleDropdownExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.selectedCircle?.name ?: "Seleccionar Núcleo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp).padding(start = 2.dp)
                )

                DropdownMenu(
                    expanded = isCircleDropdownExpanded,
                    onDismissRequest = { isCircleDropdownExpanded = false }
                ) {
                    viewModel.circles.forEach { circle ->
                        DropdownMenuItem(
                            text = { Text(circle.name) },
                            onClick = {
                                viewModel.selectCircle(circle)
                                isCircleDropdownExpanded = false
                            }
                        )
                    }
                    if (viewModel.circles.isNotEmpty()) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    }
                    DropdownMenuItem(
                        text = { Text("➕ Crear un núcleo") },
                        onClick = {
                            isCircleDropdownExpanded = false
                            navController?.navigate(Screen.Familia.route)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🔗 Unirse a un núcleo") },
                        onClick = {
                            isCircleDropdownExpanded = false
                            navController?.navigate(Screen.Familia.route)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⚙️ Administrar núcleos") },
                        onClick = {
                            isCircleDropdownExpanded = false
                            navController?.navigate(Screen.Familia.route)
                        }
                    )
                }
            }
        }

        // 2b. Floating Header: Foreground Tracking Service Switch (Aligned TopEnd)
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (viewModel.isServiceRunning) "Rastreo On" else "Rastreo Off",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isServiceRunning) PrimaryEmerald else TextMuted
                )
                Switch(
                    checked = viewModel.isServiceRunning,
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

                        if (hasLocation && hasNotifications) {
                            viewModel.toggleTrackingService(context)
                        } else {
                            val reqs = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                reqs.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionsLauncher.launch(reqs.toTypedArray())
                        }
                    },
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        // 3. Bottom Sliding Card: Members Monitoring Panel (Life360 style Expandable List)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount.y < -20f) {
                            isExpanded = true
                        } else if (dragAmount.y > 20f) {
                            isExpanded = false
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                        .clickable { isExpanded = !isExpanded }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Miembros en este Núcleo" else "Toca o desliza para ver todos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isExpanded) "▼ Colapsar" else "▲ Expandir (${viewModel.selectedCircleMembers.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (viewModel.selectedCircleMembers.isEmpty()) {
                    Text(
                        text = "Aún no hay miembros agregados en este núcleo.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    if (isExpanded) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(viewModel.selectedCircleMembers) { member ->
                                MemberRowItem(
                                    member = member,
                                    onClick = {
                                        selectedMemberForMap = member
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    } else {
                        val memberToShow = selectedMemberForMap ?: viewModel.selectedCircleMembers.firstOrNull()
                        memberToShow?.let { member ->
                            MemberRowItem(
                                member = member,
                                onClick = {
                                    selectedMemberForMap = member
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberRowItem(
    member: CircleMemberDto,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val loc = member.currentLocation

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Member Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Telemetry status
                if (loc == null) {
                    Text(text = "Sin ubicación", fontSize = 11.sp, color = TextMuted)
                } else {
                    val isOffline = loc.isOffline == true
                    val isTrackingOff = loc.isTrackingActive == false
                    val isGpsOff = loc.gpsEnabled == false

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            isTrackingOff -> Text("Rastreo Apagado", fontSize = 10.sp, color = TextMuted)
                            isGpsOff -> Text("GPS Apagado", fontSize = 10.sp, color = PrimaryRed, fontWeight = FontWeight.Bold)
                            isOffline -> Text("Sin Señal", fontSize = 10.sp, color = TextSecondary)
                            else -> {
                                if (loc.isDriving == true) {
                                    val speedVal = loc.speed ?: 0.0f
                                    Text(
                                        text = "🚗 ${speedVal.toInt()} km/h",
                                        fontSize = 10.sp,
                                        color = if (speedVal > 120.0f) PrimaryRed else PrimaryTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    val timeStr = formatLastSeen(loc.lastSeenAt)
                                    Text(text = "Visto: $timeStr", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }

                        // Battery
                        val pct = (loc.batteryLevel ?: 1.0f) * 100
                        val isBatteryLow = loc.isBatteryLow == true
                        Text(
                            text = "⚡ ${pct.toInt()}%",
                            fontSize = 10.sp,
                            color = if (isBatteryLow) PrimaryRed else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Direction Intent button
            if (loc != null) {
                IconButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${loc.latitude},${loc.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "Cómo llegar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatLastSeen(isoTimestamp: String?): String {
    if (isoTimestamp == null) return "Nunca"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val inputFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = try {
            inputFormat.parse(isoTimestamp)
        } catch (e: Exception) {
            inputFallback.parse(isoTimestamp)
        }
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        "Reciente"
    }
}
