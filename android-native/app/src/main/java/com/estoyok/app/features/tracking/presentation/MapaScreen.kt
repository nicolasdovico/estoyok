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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
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
import com.estoyok.app.features.tracking.data.model.GeofenceDto
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    navController: NavHostController? = null,
    viewModel: MapaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val contentResolver = context.contentResolver
            try {
                val inputStream = contentResolver.openInputStream(selectedUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes != null) {
                    val mimeType = contentResolver.getType(selectedUri) ?: "image/jpeg"
                    val fileExtension = when (mimeType) {
                        "image/png" -> "png"
                        else -> "jpg"
                    }
                    val requestFile = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val avatarPart = okhttp3.MultipartBody.Part.createFormData(
                        "avatar",
                        "avatar_$fileExtension",
                        requestFile
                    )
                    viewModel.uploadAvatar(avatarPart)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer la imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
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
            viewModel.toggleTrackingService(context)
        } else {
            Toast.makeText(context, "Se necesitan permisos de ubicación y notificaciones para el rastreo", Toast.LENGTH_LONG).show()
        }
    }
    var isCircleDropdownExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var showCreateGeofenceDialog by remember { mutableStateOf(false) }
    var longClickedLatLng by remember { mutableStateOf<LatLng?>(null) }

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

    LaunchedEffect(viewModel.historyPoints, viewModel.selectedTripIndex) {
        if (viewModel.historyPoints.isNotEmpty()) {
            val segments = segmentHistoryPoints(viewModel.historyPoints)
            val pointsToFit = if (viewModel.selectedTripIndex != null && viewModel.selectedTripIndex!! < segments.size) {
                segments[viewModel.selectedTripIndex!!].points
            } else {
                viewModel.historyPoints
            }

            if (pointsToFit.isNotEmpty()) {
                val builder = LatLngBounds.builder()
                pointsToFit.forEach { point ->
                    builder.include(LatLng(point.latitude, point.longitude))
                }
                val bounds = builder.build()
                try {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds, 150)
                    )
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    var lastFittedCircleId by remember { mutableStateOf<Int?>(null) }

    val fitAllMembers: () -> Unit = {
        val membersWithLocation = viewModel.selectedCircleMembers.filter { it.currentLocation != null }
        if (membersWithLocation.isNotEmpty()) {
            scope.launch {
                if (membersWithLocation.size == 1) {
                    val loc = membersWithLocation.first().currentLocation!!
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(loc.latitude, loc.longitude),
                                15f
                            )
                        )
                    } catch (e: Exception) {
                        // ignore if map not ready
                    }
                } else {
                    val builder = LatLngBounds.builder()
                    membersWithLocation.forEach { member ->
                        val loc = member.currentLocation!!
                        builder.include(LatLng(loc.latitude, loc.longitude))
                    }
                    val bounds = builder.build()
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 150)
                        )
                    } catch (e: Exception) {
                        try {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngBounds(bounds, 150)
                            )
                        } catch (ex: Exception) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    // Auto-fit group coordinates whenever active circle changes
    LaunchedEffect(viewModel.selectedCircle, viewModel.selectedCircleMembers) {
        val circleId = viewModel.selectedCircle?.id
        if (circleId != null && circleId != lastFittedCircleId && viewModel.selectedCircleMembers.isNotEmpty()) {
            val hasLocation = viewModel.selectedCircleMembers.any { it.currentLocation != null }
            if (hasLocation) {
                lastFittedCircleId = circleId
                fitAllMembers()
            }
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
                myLocationButtonEnabled = false
            ),
            contentPadding = PaddingValues(bottom = 120.dp),
            onMapLongClick = { latLng ->
                longClickedLatLng = latLng
                showCreateGeofenceDialog = true
            }
        ) {
            // Render location history route if loaded
            if (viewModel.historyPoints.isNotEmpty()) {
                val segments = remember(viewModel.historyPoints) {
                    segmentHistoryPoints(viewModel.historyPoints)
                }

                if (viewModel.selectedTripIndex != null && viewModel.selectedTripIndex!! < segments.size) {
                    val trip = segments[viewModel.selectedTripIndex!!]
                    val path = trip.points.map { LatLng(it.latitude, it.longitude) }
                    Polyline(
                        points = path,
                        color = MaterialTheme.colorScheme.primary,
                        width = 10f
                    )

                    val startPoint = path.first()
                    val endPoint = path.last()

                    Marker(
                        state = rememberMarkerState(position = startPoint),
                        title = "Inicio de viaje",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )

                    Marker(
                        state = rememberMarkerState(position = endPoint),
                        title = "Fin de viaje",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                } else {
                    segments.forEach { segment ->
                        val path = segment.points.map { LatLng(it.latitude, it.longitude) }
                        Polyline(
                            points = path,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            width = 6f
                        )
                    }
                }
            }

            // Render static Zonas Seguras
            viewModel.selectedCircle?.geofences?.forEach { geofence ->
                val centerLatLng = LatLng(geofence.latitude, geofence.longitude)
                Circle(
                    center = centerLatLng,
                    radius = geofence.radius,
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    strokeColor = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3f
                )
                Marker(
                    state = rememberMarkerState(position = centerLatLng),
                    title = "Zona Segura: ${geofence.name}",
                    snippet = "Radio: ${geofence.radius.toInt()}m",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

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

                    key(member.id) {
                        val markerState = rememberMarkerState(position = latLng)
                        LaunchedEffect(latLng) {
                            markerState.position = latLng
                        }

                        val isOffline = loc.isOffline == true
                        val isTrackingOff = loc.isTrackingActive == false
                        val isGpsOff = loc.gpsEnabled == false

                        val borderColor = when {
                            isTrackingOff || isOffline -> TextMuted
                            isGpsOff -> PrimaryOrange
                            else -> PrimaryEmerald
                        }

                        MarkerComposable(
                            state = markerState,
                            title = titleText,
                            snippet = snippetText,
                            onClick = {
                                selectedMemberForMap = member
                                false // Return false to show standard info window popup
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                // Avatar Circle Container
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(CardBackground, CircleShape)
                                        .border(2.dp, borderColor, CircleShape)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!member.avatarUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = member.avatarUrl,
                                            contentDescription = member.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val initials = member.name.split(" ")
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .take(2)
                                            .joinToString("")
                                            .uppercase()
                                        Text(
                                            text = initials,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Pointer arrow pointing down (rotated square)
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-5).dp)
                                        .size(8.dp)
                                        .graphicsLayer(rotationZ = 45f)
                                        .background(borderColor)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Small first name tag below
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = DarkSurface.copy(alpha = 0.9f)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, borderColor.copy(alpha = 0.5f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = member.name.substringBefore(" "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
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
                    onCheckedChange = { _ ->
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

        // 2c. Floating Action Button: Centrar Grupo (Fit All Members)
        FloatingActionButton(
            onClick = { fitAllMembers() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar Grupo"
            )
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
                            item {
                                Text(
                                    text = "Familiares",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(viewModel.selectedCircleMembers) { member ->
                                MemberRowItem(
                                    member = member,
                                    onClick = {
                                        selectedMemberForMap = member
                                        viewModel.selectedMember = member
                                        isExpanded = false
                                    }
                                )
                            }

                            // Show static Zonas Seguras list
                            val geofences = viewModel.selectedCircle?.geofences ?: emptyList()
                            if (geofences.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BorderColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Zonas Seguras del Núcleo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(geofences) { geofence ->
                                    GeofenceRowItem(
                                        geofence = geofence,
                                        isOwner = viewModel.selectedCircle?.ownerId == (viewModel.currentUserProfile?.id ?: -1),
                                        onDeleteClick = { viewModel.deleteGeofence(geofence.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        val memberToShow = selectedMemberForMap ?: viewModel.selectedCircleMembers.firstOrNull()
                        memberToShow?.let { member ->
                            MemberRowItem(
                                member = member,
                                onClick = {
                                    selectedMemberForMap = member
                                    viewModel.selectedMember = member
                                }
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.historyPoints.isNotEmpty()) {
            val segments = remember(viewModel.historyPoints) {
                segmentHistoryPoints(viewModel.historyPoints)
            }
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (viewModel.selectedTripIndex != null && viewModel.selectedTripIndex!! < segments.size) {
                            val trip = segments[viewModel.selectedTripIndex!!]
                            Text(
                                text = "Viaje ${viewModel.selectedTripIndex!! + 1}: ${viewModel.selectedMember?.name ?: ""}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${trip.startTime} - ${trip.endTime} • ${trip.durationText} • %.1f km".format(trip.distanceKm),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = "Historial: ${viewModel.selectedMember?.name ?: ""}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Fecha: ${viewModel.historyDate ?: ""} • ${segments.size} viajes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (viewModel.selectedTripIndex != null) {
                            Button(
                                onClick = { viewModel.selectedTripIndex = null },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Atrás", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Salir", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (viewModel.selectedMember != null && viewModel.selectedTripIndex == null) {
            val member = viewModel.selectedMember!!
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.selectedMember = null
                    viewModel.clearHistory()
                    viewModel.clearUploadMessages()
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                MemberDetailsSheetContent(
                    member = member,
                    viewModel = viewModel,
                    navController = navController,
                    imagePickerLauncher = imagePickerLauncher
                )
            }
        }

        if (showCreateGeofenceDialog && longClickedLatLng != null) {
            var geofenceName by remember { mutableStateOf("") }
            var geofenceRadius by remember { mutableStateOf(200.0) }
            var selectedMemberIdForGeofence by remember { mutableStateOf<Int?>(null) }
            var isDropdownExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    showCreateGeofenceDialog = false
                    longClickedLatLng = null
                },
                title = { Text("Nueva Zona Segura", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Crearás un perímetro seguro en la ubicación seleccionada.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = geofenceName,
                            onValueChange = { geofenceName = it },
                            label = { Text("Nombre (ej. Casa, Trabajo)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        Column {
                            Text(
                                text = "Radio: ${geofenceRadius.toInt()} metros",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Slider(
                                value = geofenceRadius.toFloat(),
                                onValueChange = { geofenceRadius = it.toDouble() },
                                valueRange = 50f..1000f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { isDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                val memberName = selectedMemberIdForGeofence?.let { id ->
                                    viewModel.selectedCircleMembers.find { it.id == id }?.name
                                } ?: "Toda la familia"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Asignar a: $memberName", fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                                }
                            }

                            DropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Toda la familia", color = TextPrimary) },
                                    onClick = {
                                        selectedMemberIdForGeofence = null
                                        isDropdownExpanded = false
                                    }
                                )
                                viewModel.selectedCircleMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.name, color = TextPrimary) },
                                        onClick = {
                                            selectedMemberIdForGeofence = member.id
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (geofenceName.isNotBlank()) {
                                viewModel.createGeofence(
                                    name = geofenceName,
                                    radius = geofenceRadius,
                                    latitude = longClickedLatLng!!.latitude,
                                    longitude = longClickedLatLng!!.longitude,
                                    userId = selectedMemberIdForGeofence
                                )
                                showCreateGeofenceDialog = false
                                longClickedLatLng = null
                            } else {
                                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        Text("Crear", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCreateGeofenceDialog = false
                        longClickedLatLng = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        LaunchedEffect(viewModel.geofenceSuccessMessage, viewModel.geofenceErrorMessage) {
            viewModel.geofenceSuccessMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearGeofenceMessages()
            }
            viewModel.geofenceErrorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearGeofenceMessages()
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
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!member.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = member.avatarUrl,
                        contentDescription = "Avatar de ${member.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initials = member.name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailsSheetContent(
    member: CircleMemberDto,
    viewModel: MapaViewModel,
    navController: NavHostController?,
    imagePickerLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>
) {
    var showPremiumPromoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isSelf = member.id == (viewModel.currentUserProfile?.id ?: -1)
    val isPremium = viewModel.currentUserProfile?.isPremium == true

    val dates = remember {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        (0..29).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            Triple(
                dateFormat.format(cal.time),
                displayFormat.format(cal.time),
                offset == 0
            )
        }
    }

    if (showPremiumPromoDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumPromoDialog = false },
            title = { Text("🔒 Historial Extendido") },
            text = { Text("El acceso al historial de recorridos de los últimos 30 días es exclusivo para cuentas Premium. ¡Mejora tu plan para proteger a los tuyos!") },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumPromoDialog = false
                        viewModel.selectedMember = null
                        navController?.navigate(Screen.Premium.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("Ver Planes Premium", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumPromoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with edit option if it's self
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!member.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = member.avatarUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initials = member.name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                Text(
                    text = initials,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isSelf) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.isUploadingAvatar) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Foto",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Upload results toast / message
        LaunchedEffect(viewModel.uploadAvatarSuccessMessage, viewModel.uploadAvatarErrorMessage) {
            viewModel.uploadAvatarSuccessMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearUploadMessages()
            }
            viewModel.uploadAvatarErrorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearUploadMessages()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = member.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = member.email,
            fontSize = 14.sp,
            color = TextSecondary
        )

        member.phone?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(16.dp))

        // History Title
        Text(
            text = "Historial de Recorridos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Dates Selector
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(dates) { (dateStr, displayStr, isFreeAllowed) ->
                val isSelectable = isPremium || isFreeAllowed
                val isSelected = viewModel.historyDate == dateStr

                Card(
                    modifier = Modifier
                        .width(72.dp)
                        .height(80.dp)
                        .clickable {
                            if (isSelectable) {
                                viewModel.loadMemberHistory(member.id, dateStr)
                            } else {
                                showPremiumPromoDialog = true
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            !isSelectable -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val dayText = if (dateStr == dates.first().first) "Hoy" else displayStr.split(" ").firstOrNull() ?: ""
                        Text(
                            text = dayText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else if (isSelectable) TextPrimary else TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val monthText = displayStr.split(" ").lastOrNull() ?: ""
                        Text(
                            text = if (isSelectable) monthText else "$monthText 🔒",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading or status of history points
        if (viewModel.isHistoryLoading) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else if (viewModel.historyDate != null) {
            if (viewModel.historyPoints.isEmpty()) {
                Text(
                    text = "No hay registros de movimiento para el ${viewModel.historyDate}",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            } else {
                val segments = remember(viewModel.historyPoints) {
                    segmentHistoryPoints(viewModel.historyPoints)
                }

                val geofences = viewModel.selectedCircle?.geofences ?: emptyList()
                val timelineItems = remember(segments, geofences) {
                    buildHistoryTimeline(segments, geofences)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Línea de Tiempo del Recorrido",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text("Limpiar", color = PrimaryRed, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timelineItems.reversed().forEach { item ->
                        when (item) {
                            is TimelineItem.Trip -> {
                                val trip = item.trip
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedTripIndex = trip.index
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = "Viaje",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Viaje ${trip.index + 1}: ${trip.startTime} - ${trip.endTime}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Duración: ${trip.durationText} • Distancia: %.1f km".format(trip.distanceKm),
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            if (trip.maxSpeedKmh > 0.0) {
                                                Text(
                                                    text = "Velocidad Máx: ${trip.maxSpeedKmh.toInt()} km/h",
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Ver en mapa",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            is TimelineItem.SafeZoneStay -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("📍", fontSize = 18.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Estadía en: ${item.name}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Permanencia: ${item.startTime} - ${item.endTime} (${item.durationText})",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TripSegment(
    val index: Int,
    val points: List<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>,
    val startTime: String,
    val endTime: String,
    val durationText: String,
    val distanceKm: Double,
    val maxSpeedKmh: Double
)

private fun parseIsoToSeconds(isoStr: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallbackDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = try {
            format.parse(isoStr)
        } catch (e: Exception) {
            try {
                fallback.parse(isoStr)
            } catch (e2: Exception) {
                fallbackDb.parse(isoStr)
            }
        }
        date?.time?.div(1000) ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun formatTime(isoStr: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallbackDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = try {
            format.parse(isoStr)
        } catch (e: Exception) {
            try {
                fallback.parse(isoStr)
            } catch (e2: Exception) {
                fallbackDb.parse(isoStr)
            }
        }
        val output = SimpleDateFormat("HH:mm", Locale.getDefault())
        output.format(date!!)
    } catch (e: Exception) {
        "Reciente"
    }
}

private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

private fun calculateDistanceKm(points: List<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>): Double {
    var total = 0.0
    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        total += haversineDistance(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
    }
    return total
}

fun segmentHistoryPoints(points: List<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>): List<TripSegment> {
    if (points.isEmpty()) return emptyList()
    val segmentsList = mutableListOf<List<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>>()
    var currentSegment = mutableListOf<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>()

    val sortedPoints = points.sortedBy { it.recordedAt }
    for (point in sortedPoints) {
        if (currentSegment.isEmpty()) {
            currentSegment.add(point)
        } else {
            val lastPoint = currentSegment.last()
            val diffSeconds = parseIsoToSeconds(point.recordedAt) - parseIsoToSeconds(lastPoint.recordedAt)
            val dist = haversineDistance(lastPoint.latitude, lastPoint.longitude, point.latitude, point.longitude)
            // 10 minutes gap OR more than 1.5 km distance jump -> new segment
            if (diffSeconds > 600 || dist > 1.5) {
                segmentsList.add(currentSegment)
                currentSegment = mutableListOf(point)
            } else {
                currentSegment.add(point)
            }
        }
    }
    if (currentSegment.isNotEmpty()) {
        segmentsList.add(currentSegment)
    }

    return segmentsList.mapIndexed { idx, segPoints ->
        val startSecs = parseIsoToSeconds(segPoints.first().recordedAt)
        val endSecs = parseIsoToSeconds(segPoints.last().recordedAt)
        val totalSeconds = endSecs - startSecs
        val durationText = when {
            totalSeconds < 60 -> "Menos de 1 min"
            totalSeconds < 3600 -> "${totalSeconds / 60} min"
            else -> {
                val hours = totalSeconds / 3600
                val mins = (totalSeconds % 3600) / 60
                if (mins > 0) "${hours} h ${mins} min" else "${hours} h"
            }
        }
        val distance = calculateDistanceKm(segPoints)

        var maxSpeedKmh = 0.0
        for (i in 0 until segPoints.size - 1) {
            val p1 = segPoints[i]
            val p2 = segPoints[i + 1]
            val dist = haversineDistance(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
            val timeHours = (parseIsoToSeconds(p2.recordedAt) - parseIsoToSeconds(p1.recordedAt)) / 3600.0
            if (timeHours > 0.0) {
                val speed = dist / timeHours
                if (speed in 5.0..180.0 && speed > maxSpeedKmh) {
                    maxSpeedKmh = speed
                }
            }
        }

        TripSegment(
            index = idx,
            points = segPoints,
            startTime = formatTime(segPoints.first().recordedAt),
            endTime = formatTime(segPoints.last().recordedAt),
            durationText = durationText,
            distanceKm = distance,
            maxSpeedKmh = maxSpeedKmh.toDouble()
        )
    }
}

@Composable
fun GeofenceRowItem(
    geofence: GeofenceDto,
    isOwner: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = geofence.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Radio: ${geofence.radius.toInt()}m" + (geofence.userId?.let { " • Personal" } ?: " • Todos"),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            if (isOwner) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Zona Segura",
                        tint = PrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

sealed class TimelineItem {
    data class Trip(val trip: TripSegment) : TimelineItem()
    data class SafeZoneStay(
        val name: String,
        val durationText: String,
        val startTime: String,
        val endTime: String
    ) : TimelineItem()
}

fun buildHistoryTimeline(
    trips: List<TripSegment>,
    geofences: List<GeofenceDto>
): List<TimelineItem> {
    if (trips.isEmpty()) return emptyList()
    val timeline = mutableListOf<TimelineItem>()

    // Check if the user was in a safe zone before the first trip of the day
    val firstTrip = trips.first()
    val firstPoint = firstTrip.points.firstOrNull()
    if (firstPoint != null) {
        val startSafeZone = geofences.find {
            haversineDistance(firstPoint.latitude, firstPoint.longitude, it.latitude, it.longitude) * 1000 <= it.radius
        }
        if (startSafeZone != null) {
            timeline.add(
                TimelineItem.SafeZoneStay(
                    name = startSafeZone.name,
                    durationText = "Temprano",
                    startTime = "00:00",
                    endTime = firstTrip.startTime
                )
            )
        }
    }

    // Intersperse trips and safe zone stays
    for (i in trips.indices) {
        timeline.add(TimelineItem.Trip(trips[i]))

        if (i < trips.size - 1) {
            val currentTrip = trips[i]
            val nextTrip = trips[i + 1]
            val lastPoint = currentTrip.points.lastOrNull()
            val nextFirstPoint = nextTrip.points.firstOrNull()
            if (lastPoint != null && nextFirstPoint != null) {
                val staySafeZone = geofences.find {
                    haversineDistance(lastPoint.latitude, lastPoint.longitude, it.latitude, it.longitude) * 1000 <= it.radius
                }
                if (staySafeZone != null) {
                    val startSecs = parseIsoToSeconds(lastPoint.recordedAt)
                    val endSecs = parseIsoToSeconds(nextFirstPoint.recordedAt)
                    val totalSeconds = endSecs - startSecs
                    val durationText = when {
                        totalSeconds < 60 -> "Menos de 1 min"
                        totalSeconds < 3600 -> "${totalSeconds / 60} min"
                        else -> {
                            val hours = totalSeconds / 3600
                            val mins = (totalSeconds % 3600) / 60
                            if (mins > 0) "${hours} h ${mins} min" else "${hours} h"
                        }
                    }
                    timeline.add(
                        TimelineItem.SafeZoneStay(
                            name = staySafeZone.name,
                            durationText = durationText,
                            startTime = currentTrip.endTime,
                            endTime = nextTrip.startTime
                        )
                    )
                }
            }
        }
    }

    // Check if the user is in a safe zone after the last trip
    val lastTrip = trips.last()
    val lastPoint = lastTrip.points.lastOrNull()
    if (lastPoint != null) {
        val endSafeZone = geofences.find {
            haversineDistance(lastPoint.latitude, lastPoint.longitude, it.latitude, it.longitude) * 1000 <= it.radius
        }
        if (endSafeZone != null) {
            timeline.add(
                TimelineItem.SafeZoneStay(
                    name = endSafeZone.name,
                    durationText = "Tarde",
                    startTime = lastTrip.endTime,
                    endTime = "23:59"
                )
            )
        }
    }

    return timeline
}
