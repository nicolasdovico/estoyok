package com.estoyok.app.features.tracking.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
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
    viewModel: MapaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isCircleDropdownExpanded by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circle selector dropdown card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.clickable { isCircleDropdownExpanded = true }
                    ) {
                        Text(
                            text = "Núcleo Activo",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewModel.selectedCircle?.name ?: "Seleccionar Núcleo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(start = 4.dp)
                            )
                        }

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
                        }
                    }

                    // Foreground Tracking Service Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (viewModel.isServiceRunning) "Rastreo On" else "Rastreo Off",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isServiceRunning) PrimaryEmerald else TextMuted,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = viewModel.isServiceRunning,
                            onCheckedChange = { viewModel.toggleTrackingService(context) }
                        )
                    }
                }
            }
        }

        // 3. Bottom Sliding Card: Members Monitoring Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Miembros en este Núcleo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (viewModel.selectedCircleMembers.isEmpty()) {
                    Text(
                        text = "Aún no hay miembros agregados en este núcleo.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        items(viewModel.selectedCircleMembers) { member ->
                            MemberMapCard(
                                member = member,
                                onClick = { selectedMemberForMap = member }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberMapCard(
    member: CircleMemberDto,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val loc = member.currentLocation

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = member.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Telemetry statuses / badges
            if (loc == null) {
                Text(
                    text = "Sin ubicación",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            } else {
                // Check connectivity or sensor issues
                val isOffline = loc.isOffline == true
                val isTrackingOff = loc.isTrackingActive == false
                val isGpsOff = loc.gpsEnabled == false

                when {
                    isTrackingOff -> {
                        Surface(
                            color = BorderColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Rastreo Apagado",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = TextMuted
                            )
                        }
                    }
                    isGpsOff -> {
                        Surface(
                            color = PrimaryRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "GPS Desactivado",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = PrimaryRed
                            )
                        }
                    }
                    isOffline -> {
                        Surface(
                            color = BorderColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Sin Señal",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        // Show speed/driving or last seen
                        if (loc.isDriving == true) {
                            val speedVal = loc.speed ?: 0.0f
                            Surface(
                                color = if (speedVal > 120.0f) PrimaryRed.copy(alpha = 0.2f) else PrimaryTeal.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🚗 ${speedVal.toInt()} km/h",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = if (speedVal > 120.0f) PrimaryRed else PrimaryTeal
                                )
                            }
                        } else {
                            val timeStr = formatLastSeen(loc.lastSeenAt)
                            Text(
                                text = "Visto: $timeStr",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Battery levels indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val pct = (loc.batteryLevel ?: 1.0f) * 100
                    val isBatteryLow = loc.isBatteryLow == true
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Battery Status",
                        tint = if (isBatteryLow) PrimaryRed else PrimaryEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pct.toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBatteryLow) PrimaryRed else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // External Map Intents redirect button
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
