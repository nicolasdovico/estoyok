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
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import coil.imageLoader
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.location.Geocoder
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                    if (fileBytes.size > 2 * 1024 * 1024) {
                        Toast.makeText(context, "La imagen supera el límite de 2 MB. Elige una más liviana.", Toast.LENGTH_LONG).show()
                    } else {
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
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer la imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val markerBitmaps = remember { mutableStateMapOf<Int, Bitmap>() }

    LaunchedEffect(Unit) {
        snapshotFlow { Triple(viewModel.selectedCircleMembers, viewModel.avatarVersion, viewModel.currentUserProfile) }
            .collect { (members, version, profile) ->
                members.forEach { member ->
                    val url = member.avatarUrl
                    if (!url.isNullOrEmpty() && url != "null") {
                        val isCurrentUser = profile != null && member.id == profile.id
                        val finalUrl = if (isCurrentUser) {
                            "$url?v=$version"
                        } else {
                            url
                        }
                        val isCached = markerBitmaps.containsKey(member.id)
                        if (!isCached || isCurrentUser) {
                            launch {
                                try {
                                    android.util.Log.d("MapaScreen", "Downloading marker avatar for ${member.name} (isCurrentUser: $isCurrentUser) from: $finalUrl")
                                    val request = coil.request.ImageRequest.Builder(context)
                                        .data(finalUrl)
                                        .allowHardware(false)
                                        .build()
                                    val result = context.imageLoader.execute(request)
                                    if (result is coil.request.SuccessResult) {
                                        val drawable = result.drawable
                                        markerBitmaps[member.id] = drawable.toBitmap()
                                        android.util.Log.d("MapaScreen", "Successfully downloaded avatar for ${member.name}")
                                    } else {
                                        android.util.Log.e("MapaScreen", "Failed to download avatar for ${member.name}")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MapaScreen", "Error downloading avatar for ${member.name}: ${e.message}", e)
                                }
                            }
                        }
                    } else {
                        markerBitmaps.remove(member.id)
                    }
                }
            }
    }

    var showBackgroundLocationDialog by remember { mutableStateOf(false) }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Permiso de segundo plano concedido. La app te protegerá en todo momento.", Toast.LENGTH_LONG).show()
            if (!viewModel.isServiceRunning) {
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
            } else if (!viewModel.isServiceRunning) {
                viewModel.toggleTrackingService(context)
            }
        } else {
            Toast.makeText(context, "Se necesitan permisos de ubicación y notificaciones para el rastreo", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
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
            } else if (!viewModel.isServiceRunning) {
                viewModel.toggleTrackingService(context)
            }
        } else {
            val reqs = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                reqs.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionsLauncher.launch(reqs.toTypedArray())
        }
    }

    var isCircleDropdownExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var showCreateGeofenceDialog by remember { mutableStateOf(false) }
    var longClickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showEditGeofenceDialog by remember { mutableStateOf(false) }
    var geofenceToEdit by remember { mutableStateOf<GeofenceDto?>(null) }

    val stayTracker = remember { mutableStateMapOf<Int, Pair<LatLng, Long>>() }
    LaunchedEffect(viewModel.selectedCircleMembers) {
        val now = System.currentTimeMillis()
        viewModel.selectedCircleMembers.forEach { member ->
            val loc = member.currentLocation
            if (loc != null) {
                val latLng = LatLng(loc.latitude, loc.longitude)
                val isMoving = loc.isDriving == true || (loc.speed ?: 0f) >= 1.5f
                if (isMoving) {
                    stayTracker.remove(member.id)
                } else {
                    val lastTrack = stayTracker[member.id]
                    if (lastTrack == null) {
                        stayTracker[member.id] = Pair(latLng, now)
                    } else {
                        val dist = haversineDistance(
                            lastTrack.first.latitude, lastTrack.first.longitude,
                            latLng.latitude, latLng.longitude
                        )
                        if (dist > 0.03) { // 30 meters
                            stayTracker[member.id] = Pair(latLng, now)
                        }
                    }
                }
            } else {
                stayTracker.remove(member.id)
            }
        }
    }


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
                15.5f
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
                                15.5f
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
                    val center = bounds.center
                    val distance = haversineDistance(
                        bounds.southwest.latitude, bounds.southwest.longitude,
                        bounds.northeast.latitude, bounds.northeast.longitude
                    )

                    try {
                        if (distance < 1.0) { // If members are closer than 1 km
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(center, 15.5f)
                            )
                        } else {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 150)
                            )
                        }
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
                myLocationButtonEnabled = false,
                compassEnabled = false,
                mapToolbarEnabled = false
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

                    val isOffline = loc.isOffline == true
                    val isTrackingOff = loc.isTrackingActive == false
                    val isGpsOff = loc.gpsEnabled == false

                    val borderColor = when {
                        isTrackingOff || isOffline -> TextMuted
                        isGpsOff -> PrimaryOrange
                        else -> PrimaryEmerald
                    }

                    val hasBitmap = markerBitmaps[member.id] != null
                    val movementEmoji = if (isOffline || isTrackingOff || isGpsOff) {
                        null
                    } else {
                        getMovementEmoji(loc.speed, loc.isDriving)
                    }

                    val subtitleText = run {
                        when {
                            isTrackingOff -> "Rastreo Apagado"
                            isGpsOff -> "GPS Apagado"
                            isOffline -> "Sin Señal"
                            else -> {
                                val isD = loc.isDriving == true
                                val speedKmh = loc.speed ?: 0.0f
                                if (isD || speedKmh >= 15.0f) {
                                    "${speedKmh.toInt()} km/h"
                                } else {
                                    val stayInfo = stayTracker[member.id]
                                    if (stayInfo != null) {
                                        val durationMs = System.currentTimeMillis() - stayInfo.second
                                        val durationMins = durationMs / 60000L
                                        if (durationMins > 0) {
                                            if (durationMins >= 60) {
                                                val hours = durationMins / 60
                                                val mins = durationMins % 60
                                                if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
                                            } else {
                                                "${durationMins} min"
                                            }
                                        } else {
                                            "Reciente"
                                        }
                                    } else {
                                        null
                                    }
                                }
                            }
                        }
                    }

                    key(member.id, hasBitmap, borderColor, movementEmoji, subtitleText) {
                        val markerState = rememberMarkerState(position = latLng)
                        LaunchedEffect(latLng) {
                            val startLatLng = markerState.position
                            val endLatLng = latLng
                            if (startLatLng.latitude != endLatLng.latitude || startLatLng.longitude != endLatLng.longitude) {
                                val duration = 1500L
                                val startTime = System.currentTimeMillis()
                                while (true) {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    val t = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                                    val lat = startLatLng.latitude + (endLatLng.latitude - startLatLng.latitude) * t
                                    val lng = startLatLng.longitude + (endLatLng.longitude - startLatLng.longitude) * t
                                    markerState.position = LatLng(lat, lng)
                                    if (t >= 1f) break
                                    kotlinx.coroutines.delay(16)
                                }
                            } else {
                                markerState.position = latLng
                            }
                        }

                        MarkerComposable(
                            state = markerState,
                            title = titleText,
                            snippet = snippetText,
                            onClick = {
                                selectedMemberForMap = member
                                viewModel.selectedMember = member
                                true
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                if (!subtitleText.isNullOrEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = DarkSurface.copy(alpha = 0.95f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.8f)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Text(
                                            text = subtitleText,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                }

                                // Unified Pin shape container (Avatar + Pointer Tail + Movement Badge)
                                Box(
                                    modifier = Modifier
                                        .size(width = 56.dp, height = 56.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    // 1. Pointer tail (rotated square) centered at the bottom
                                    Box(
                                        modifier = Modifier
                                            .padding(bottom = 6.dp)
                                            .size(14.dp)
                                            .graphicsLayer(rotationZ = 45f)
                                            .background(borderColor)
                                    )

                                    // 2. Avatar circle container pushed to the top
                                    Box(
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .size(48.dp)
                                            .align(Alignment.TopCenter)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(borderColor, CircleShape)
                                                .padding(3.dp), // Solid 3dp thick border
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val initials = member.name.split(" ")
                                                .mapNotNull { it.firstOrNull()?.toString() }
                                                .take(2)
                                                .joinToString("")
                                                .uppercase()

                                            val bitmapToDraw = markerBitmaps[member.id]
                                            if (bitmapToDraw != null) {
                                                Image(
                                                    bitmap = bitmapToDraw.asImageBitmap(),
                                                    contentDescription = member.name,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(CardBackground, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initials,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 3. Movement badge overlay (larger and clearer) aligned at bottom right
                                    if (movementEmoji != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.BottomEnd)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = movementEmoji, fontSize = 13.sp)
                                        }
                                    }
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


        // Floating switch removed to clean up the map, re-located to Settings

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
                                        onEditClick = {
                                            geofenceToEdit = geofence
                                            showEditGeofenceDialog = true
                                        },
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

        if (!isExpanded) {
            // 2c. Floating Action Button: Centrar Grupo (Fit All Members)
            FloatingActionButton(
                onClick = { fitAllMembers() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 145.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar Grupo"
                )
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
                    imagePickerLauncher = imagePickerLauncher,
                    onCreateGeofence = { latLng ->
                        longClickedLatLng = latLng
                        showCreateGeofenceDialog = true
                    },
                    onEditGeofence = { geofence ->
                        geofenceToEdit = geofence
                        showEditGeofenceDialog = true
                    }
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

        if (showEditGeofenceDialog && geofenceToEdit != null) {
            val geofence = geofenceToEdit!!
            var geofenceName by remember { mutableStateOf(geofence.name) }
            var geofenceRadius by remember { mutableStateOf(geofence.radius) }
            var selectedMemberIdForGeofence by remember { mutableStateOf<Int?>(geofence.userId) }
            var isDropdownExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    showEditGeofenceDialog = false
                    geofenceToEdit = null
                },
                title = { Text("Editar Zona Segura", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Modifica los parámetros de la zona segura.",
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
                                viewModel.updateGeofence(
                                    geofenceId = geofence.id,
                                    name = geofenceName,
                                    radius = geofenceRadius,
                                    userId = selectedMemberIdForGeofence
                                )
                                showEditGeofenceDialog = false
                                geofenceToEdit = null
                            } else {
                                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditGeofenceDialog = false
                        geofenceToEdit = null
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
fun MemberRowItem(
    member: CircleMemberDto,
    onClick: () -> Unit,
    viewModel: MapaViewModel = hiltViewModel()
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
            // Avatar wrapped with Box for movement badge overlay
            Box(
                modifier = Modifier.size(width = 48.dp, height = 44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopCenter)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val rowAvatarUrl = if (!member.avatarUrl.isNullOrEmpty() && member.avatarUrl != "null") {
                        if (member.id == viewModel.currentUserProfile?.id) {
                            "${member.avatarUrl}?v=${viewModel.avatarVersion}"
                        } else {
                            member.avatarUrl
                        }
                    } else {
                        null
                    }

                    val initials = member.name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    SubcomposeAsyncImage(
                        model = rowAvatarUrl,
                        contentDescription = "Avatar de ${member.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    )
                }

                if (loc != null) {
                    val movementEmoji = getMovementEmoji(loc.speed, loc.isDriving)
                    if (movementEmoji != null) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = movementEmoji, fontSize = 9.sp)
                        }
                    }
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
    imagePickerLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    onCreateGeofence: (LatLng) -> Unit,
    onEditGeofence: (GeofenceDto) -> Unit
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

    LaunchedEffect(member.id) {
        val todayStr = dates.first().first
        viewModel.loadMemberHistory(member.id, todayStr)
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
            val detailsAvatarUrl = if (!member.avatarUrl.isNullOrEmpty() && member.avatarUrl != "null") {
                if (member.id == viewModel.currentUserProfile?.id) {
                    "${member.avatarUrl}?v=${viewModel.avatarVersion}"
                } else {
                    member.avatarUrl
                }
            } else {
                null
            }

            val initials = member.name.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()

            SubcomposeAsyncImage(
                model = detailsAvatarUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

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
                val timelineItems = remember(segments, viewModel.historyPoints) {
                    buildHistoryTimeline(segments, viewModel.historyPoints)
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
                                        val icon = when (trip.transportMode) {
                                            "vehiculo" -> Icons.Default.DirectionsCar
                                            "bicicleta" -> Icons.AutoMirrored.Filled.DirectionsBike
                                            "caminando" -> Icons.AutoMirrored.Filled.DirectionsWalk
                                            else -> Icons.Default.DirectionsCar
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = "Viaje",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                if (trip.points.isNotEmpty()) {
                                                    val startPoint = trip.points.first()
                                                    val endPoint = trip.points.last()
                                                    AddressText(
                                                        latitude = startPoint.latitude,
                                                        longitude = startPoint.longitude,
                                                        geofences = geofences,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = " → ",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    AddressText(
                                                        latitude = endPoint.latitude,
                                                        longitude = endPoint.longitude,
                                                        geofences = geofences,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Viaje ${trip.index + 1}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${trip.startTime} - ${trip.endTime} • ${trip.durationText} • %.1f km".format(trip.distanceKm),
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                val modeLabel = when (trip.transportMode) {
                                                    "vehiculo" -> "Vehículo"
                                                    "bicicleta" -> "Bicicleta"
                                                    "caminando" -> "Caminata"
                                                    else -> "Viaje"
                                                }
                                                val modeColor = when (trip.transportMode) {
                                                    "vehiculo" -> MaterialTheme.colorScheme.primary
                                                    "bicicleta" -> PrimaryTeal
                                                    else -> Color(0xFFEAB308) // Amber/Yellow
                                                }
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text(modeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        labelColor = modeColor,
                                                        containerColor = modeColor.copy(alpha = 0.1f)
                                                    ),
                                                    border = BorderStroke(1.dp, modeColor.copy(alpha = 0.3f))
                                                )
                                                if (trip.maxSpeedKmh > 0.0) {
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text("Vel. máx: ${trip.maxSpeedKmh.toInt()} km/h", fontSize = 9.sp) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                                            labelColor = TextMuted,
                                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                        ),
                                                        border = BorderStroke(1.dp, BorderColor)
                                                    )
                                                }
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
                            is TimelineItem.Stay -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val matchedGeofence = geofences.find {
                                            haversineDistance(item.latitude, item.longitude, it.latitude, it.longitude) * 1000 <= it.radius
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (matchedGeofence != null) "🏠" else "📍",
                                                fontSize = 20.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Estadía en: ",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                                AddressText(
                                                    latitude = item.latitude,
                                                    longitude = item.longitude,
                                                    geofences = geofences,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Permanencia: ${item.startTime} - ${item.endTime} (${item.durationText})",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        if (matchedGeofence == null) {
                                            IconButton(
                                                onClick = {
                                                    onCreateGeofence(LatLng(item.latitude, item.longitude))
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = "Definir Zona Segura",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        } else {
                                            val isCircleOwner = viewModel.selectedCircle?.ownerId == (viewModel.currentUserProfile?.id ?: -1)
                                            if (isCircleOwner) {
                                                IconButton(
                                                    onClick = {
                                                        onEditGeofence(matchedGeofence)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Editar Zona Segura",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
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
    }
}

data class TripSegment(
    val index: Int,
    val points: List<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>,
    val startTime: String,
    val endTime: String,
    val durationText: String,
    val distanceKm: Double,
    val maxSpeedKmh: Double,
    val transportMode: String
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

            // Speed in km/h between A and B
            val impliedSpeed = if (diffSeconds > 0) dist / (diffSeconds / 3600.0) else 0.0

            var shouldSplit = false
            if (diffSeconds > 600) {
                // Scenario 1: more than 10 minutes gap
                shouldSplit = true
            } else if (diffSeconds > 300 && dist < 0.20) {
                // Scenario 2: more than 5 minutes gap AND less than 200m moved (stationary/stay)
                shouldSplit = true
            } else if (impliedSpeed > 180.0) {
                // Scenario 3: physically impossible speed (GPS jump/teleportation glitch)
                shouldSplit = true
            }

            if (shouldSplit) {
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

    val allSegments = segmentsList.mapIndexed { idx, segPoints ->
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
                if (speed in 1.5..180.0 && speed > maxSpeedKmh) {
                    maxSpeedKmh = speed
                }
            }
        }

        val mode = when {
            segPoints.any { it.isDriving == true } -> "vehiculo"
            maxSpeedKmh > 25.0 -> "vehiculo"
            maxSpeedKmh > 8.0 -> "bicicleta"
            else -> "caminando"
        }

        TripSegment(
            index = idx,
            points = segPoints,
            startTime = formatTime(segPoints.first().recordedAt),
            endTime = formatTime(segPoints.last().recordedAt),
            durationText = durationText,
            distanceKm = distance,
            maxSpeedKmh = maxSpeedKmh,
            transportMode = mode
        )
    }

    // Filter out walking micro-movements of less than 150 meters (0.15 km)
    val filteredSegments = allSegments.filter { segment ->
        !(segment.transportMode == "caminando" && segment.distanceKm < 0.15)
    }

    // Re-index remaining segments to be sequential (0, 1, 2...) for UI selection consistency
    return filteredSegments.mapIndexed { newIdx, segment ->
        segment.copy(index = newIdx)
    }
}

@Composable
fun GeofenceRowItem(
    geofence: GeofenceDto,
    isOwner: Boolean,
    onEditClick: () -> Unit,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Zona Segura",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
}

sealed class TimelineItem {
    data class Trip(val trip: TripSegment) : TimelineItem()
    data class Stay(
        val latitude: Double,
        val longitude: Double,
        val durationText: String,
        val startTime: String,
        val endTime: String
    ) : TimelineItem()
}

private fun getStartOfDaySeconds(isoStr: String): Long {
    return try {
        val formatInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallbackDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = try {
            formatInput.parse(isoStr)
        } catch (e: Exception) {
            try {
                fallback.parse(isoStr)
            } catch (e2: Exception) {
                fallbackDb.parse(isoStr)
            }
        } ?: Date()
        
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis / 1000
    } catch (e: Exception) {
        0L
    }
}

private fun getEndOfDaySeconds(isoStr: String): Long {
    return try {
        val formatInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fallbackDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = try {
            formatInput.parse(isoStr)
        } catch (e: Exception) {
            try {
                fallback.parse(isoStr)
            } catch (e2: Exception) {
                fallbackDb.parse(isoStr)
            }
        } ?: Date()
        
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        cal.timeInMillis / 1000
    } catch (e: Exception) {
        0L
    }
}

private fun formatDuration(totalSeconds: Long): String {
    return when {
        totalSeconds < 60 -> "Menos de 1 min"
        totalSeconds < 3600 -> "${totalSeconds / 60} min"
        else -> {
            val hours = totalSeconds / 3600
            val mins = (totalSeconds % 3600) / 60
            if (mins > 0) "${hours} h ${mins} min" else "${hours} h"
        }
    }
}

private fun getMovementEmoji(speed: Float?, isDriving: Boolean?): String? {
    val isD = isDriving == true
    val s = speed ?: 0.0f
    return when {
        isD || s >= 15.0f -> "🚗"
        s >= 5.0f -> "🚲"
        s >= 1.5f -> "🚶"
        else -> null
    }
}

private fun isTodayLocal(isoStr: String): Boolean {
    return try {
        val formatInput = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val fallback = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val fallbackDb = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = try {
            formatInput.parse(isoStr)
        } catch (e: Exception) {
            try {
                fallback.parse(isoStr)
            } catch (e2: Exception) {
                fallbackDb.parse(isoStr)
            }
        } ?: return false

        val calPoint = java.util.Calendar.getInstance().apply { time = date }
        val calToday = java.util.Calendar.getInstance()
        calPoint.get(java.util.Calendar.YEAR) == calToday.get(java.util.Calendar.YEAR) &&
                calPoint.get(java.util.Calendar.DAY_OF_YEAR) == calToday.get(java.util.Calendar.DAY_OF_YEAR)
    } catch (e: Exception) {
        false
    }
}

fun buildHistoryTimeline(
    trips: List<TripSegment>,
    allPoints: List<com.estoyok.app.features.tracking.data.model.LocationHistoryDto>
): List<TimelineItem> {
    if (trips.isEmpty()) {
        if (allPoints.isNotEmpty()) {
            val firstPoint = allPoints.first()
            val isToday = isTodayLocal(firstPoint.recordedAt)
            return listOf(
                TimelineItem.Stay(
                    latitude = firstPoint.latitude,
                    longitude = firstPoint.longitude,
                    durationText = if (isToday) "Todo el día (hasta ahora)" else "Todo el día",
                    startTime = "00:00",
                    endTime = if (isToday) "Ahora" else "23:59"
                )
            )
        }
        return emptyList()
    }
    val timeline = mutableListOf<TimelineItem>()

    // Check stay before the first trip
    val firstTrip = trips.first()
    val firstPoint = firstTrip.points.firstOrNull()
    if (firstPoint != null) {
        val startSecs = parseIsoToSeconds(firstTrip.points.first().recordedAt)
        val startOfDaySecs = getStartOfDaySeconds(firstTrip.points.first().recordedAt)
        val gap = startSecs - startOfDaySecs
        if (gap >= 300) { // 5 minutes threshold
            timeline.add(
                TimelineItem.Stay(
                    latitude = firstPoint.latitude,
                    longitude = firstPoint.longitude,
                    durationText = formatDuration(gap),
                    startTime = "00:00",
                    endTime = firstTrip.startTime
                )
            )
        }
    }

    // Intersperse trips and stays
    for (i in trips.indices) {
        timeline.add(TimelineItem.Trip(trips[i]))

        if (i < trips.size - 1) {
            val currentTrip = trips[i]
            val nextTrip = trips[i + 1]
            val lastPoint = currentTrip.points.lastOrNull()
            val nextFirstPoint = nextTrip.points.firstOrNull()
            if (lastPoint != null && nextFirstPoint != null) {
                val startSecs = parseIsoToSeconds(lastPoint.recordedAt)
                val endSecs = parseIsoToSeconds(nextFirstPoint.recordedAt)
                val gap = endSecs - startSecs
                if (gap >= 300) { // 5 minutes threshold
                    timeline.add(
                        TimelineItem.Stay(
                            latitude = lastPoint.latitude,
                            longitude = lastPoint.longitude,
                            durationText = formatDuration(gap),
                            startTime = currentTrip.endTime,
                            endTime = nextTrip.startTime
                        )
                    )
                }
            }
        }
    }

    // Check stay after the last trip
    val lastTrip = trips.last()
    val lastPoint = lastTrip.points.lastOrNull()
    if (lastPoint != null) {
        val lastPointSecs = parseIsoToSeconds(lastPoint.recordedAt)
        val isToday = isTodayLocal(lastPoint.recordedAt)
        val gap = if (isToday) {
            val currentSecs = System.currentTimeMillis() / 1000
            currentSecs - lastPointSecs
        } else {
            val endOfDaySecs = getEndOfDaySeconds(lastPoint.recordedAt)
            endOfDaySecs - lastPointSecs
        }
        if (gap >= 300) { // 5 minutes threshold
            timeline.add(
                TimelineItem.Stay(
                    latitude = lastPoint.latitude,
                    longitude = lastPoint.longitude,
                    durationText = if (isToday) "Desde hace ${formatDuration(gap)}" else formatDuration(gap),
                    startTime = lastTrip.endTime,
                    endTime = if (isToday) "Ahora" else "23:59"
                )
            )
        }
    }

    return timeline
}

suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare ?: ""
                val number = address.subThoroughfare ?: ""
                if (street.isNotEmpty()) {
                    if (number.isNotEmpty()) "$street $number" else street
                } else {
                    val admin = address.locality ?: address.subLocality ?: ""
                    val feature = address.featureName ?: ""
                    if (admin.isNotEmpty()) {
                        if (feature.isNotEmpty() && feature != number) "$feature, $admin" else admin
                    } else {
                        address.getAddressLine(0) ?: "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                    }
                }
            } else {
                "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
            }
        } catch (e: Exception) {
            "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
        }
    }
}

@Composable
fun AddressText(
    latitude: Double,
    longitude: Double,
    geofences: List<GeofenceDto>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textAlign: TextAlign? = null
) {
    val context = LocalContext.current
    var address by remember(latitude, longitude) { mutableStateOf("Cargando ubicación...") }

    LaunchedEffect(latitude, longitude, geofences) {
        val matchedGeofence = geofences.find {
            haversineDistance(latitude, longitude, it.latitude, it.longitude) * 1000 <= it.radius
        }
        if (matchedGeofence != null) {
            address = matchedGeofence.name
        } else {
            address = getAddressFromLocation(context, latitude, longitude)
        }
    }

    Text(
        text = address,
        modifier = modifier,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize,
        textAlign = textAlign
    )
}
