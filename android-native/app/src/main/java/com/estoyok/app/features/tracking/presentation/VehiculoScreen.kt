package com.estoyok.app.features.tracking.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.estoyok.app.core.theme.*
import com.estoyok.app.core.navigation.Screen
import com.estoyok.app.features.tracking.data.model.CircleMemberDto
import com.estoyok.app.features.tracking.data.model.MemberDriveEventDto
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Date
import java.util.Calendar

data class WeekRange(
    val id: Int,
    val label: String,
    val startDate: Date,
    val endDate: Date
)

fun getWeeks(): List<WeekRange> {
    val weeks = mutableListOf<WeekRange>()
    val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES"))
    
    for (i in 0..3) {
        val calStart = Calendar.getInstance()
        calStart.firstDayOfWeek = Calendar.MONDAY
        calStart.add(Calendar.WEEK_OF_YEAR, -i)
        
        calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calStart.set(Calendar.HOUR_OF_DAY, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)
        calStart.set(Calendar.MILLISECOND, 0)
        val startDate = calStart.time

        val calEnd = Calendar.getInstance()
        calEnd.firstDayOfWeek = Calendar.MONDAY
        calEnd.add(Calendar.WEEK_OF_YEAR, -i)
        
        calEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calEnd.set(Calendar.HOUR_OF_DAY, 23)
        calEnd.set(Calendar.MINUTE, 59)
        calEnd.set(Calendar.SECOND, 59)
        calEnd.set(Calendar.MILLISECOND, 999)
        val endDate = calEnd.time
        
        val label = when (i) {
            0 -> "Semana actual"
            1 -> "Semana anterior"
            else -> "${sdf.format(startDate)} - ${sdf.format(endDate)}"
        }
        
        weeks.add(WeekRange(i, label, startDate, endDate))
    }
    return weeks
}

enum class ExplanationType {
    NONE, SPEEDING, DISTRACTION, ACCELERATION, BRAKING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoScreen(
    navController: androidx.navigation.NavHostController? = null,
    viewModel: MapaViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val selectedCircle = viewModel.selectedCircle
    val selectedMember = viewModel.selectedMember
    val isPremium = viewModel.isPremiumDrives
    val isLoading = viewModel.isDrivesLoading
    val errorMessage = viewModel.drivesErrorMessage

    var activeDetailDrive by remember { mutableStateOf<MemberDriveEventDto?>(null) }
    
    val weeks = remember { getWeeks() }
    var selectedWeekIndex by remember { mutableStateOf(0) }
    var activeExplanationDialog by remember { mutableStateOf(ExplanationType.NONE) }

    val allFilteredDrives = remember(viewModel.allMembersDrives, selectedWeekIndex) {
        val week = weeks[selectedWeekIndex]
        val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        viewModel.allMembersDrives.mapValues { (_, raw) ->
            val drivesGrouped = groupAndMergeDrives(raw)
            drivesGrouped.filter { drive ->
                try {
                    val cleanTime = drive.startTime.replace("Z", "")
                    val driveDate = sdfIso.parse(cleanTime)
                    driveDate != null && driveDate.after(week.startDate) && driveDate.before(week.endDate)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    val consolidatedDrives = remember(allFilteredDrives) {
        allFilteredDrives.values.flatten()
    }

    val filteredDrives = remember(allFilteredDrives, selectedMember) {
        selectedMember?.let { allFilteredDrives[it.id] } ?: emptyList()
    }

    LaunchedEffect(selectedCircle) {
        if (selectedCircle != null) {
            viewModel.loadAllMembersDrives(selectedCircle.id, selectedCircle.members)
            
            if (selectedMember == null) {
                val myMember = selectedCircle.members.find { it.email == viewModel.currentUserProfile?.email }
                    ?: selectedCircle.members.firstOrNull()
                if (myMember != null) {
                    viewModel.selectedMember = myMember
                }
            }
        }
    }

    LaunchedEffect(selectedMember) {
        if (selectedMember != null && selectedCircle != null) {
            viewModel.loadMemberDrives(selectedMember.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp, top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            Text(
                text = "🚗 Protección Vehicular",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (selectedCircle == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes ningún círculo de confianza seleccionado o activo.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Main Scrollable Area
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Week Selector (Pills)
                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(weeks) { week ->
                                val isSelected = selectedWeekIndex == week.id
                                Card(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            selectedWeekIndex = week.id
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) PrimaryEmerald else CardBackground
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    border = if (isSelected) null else BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text(
                                        text = week.label,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryEmerald)
                            }
                        }
                    } else if (errorMessage != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = PrimaryRed,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        // Weekly driving stats (shown to all users consolidated)
                        item {
                            val avgScore = if (consolidatedDrives.isNotEmpty()) {
                                consolidatedDrives.map { it.safetyScore }.average().toInt()
                            } else {
                                100
                            }
                            val totalDistance = consolidatedDrives.sumOf { it.distanceKm }
                            val maxSpeedEver = consolidatedDrives.maxOfOrNull { it.maxSpeed } ?: 0.0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Resumen Semanal de Conducción",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Circular score indicator
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(80.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { avgScore / 100f },
                                                modifier = Modifier.fillMaxSize(),
                                                color = when {
                                                    avgScore >= 90 -> PrimaryEmerald
                                                    avgScore >= 70 -> PrimaryOrange
                                                    else -> PrimaryRed
                                                },
                                                strokeWidth = 8.dp,
                                                trackColor = BorderColor
                                            )
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$avgScore",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Score",
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }

                                        // Text Stats
                                        Column(
                                            modifier = Modifier.weight(1f).padding(start = 24.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Viajes totales:", color = TextMuted, fontSize = 12.sp)
                                                Text("${consolidatedDrives.size}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Distancia total:", color = TextMuted, fontSize = 12.sp)
                                                Text("${String.format(Locale.US, "%.1f", totalDistance)} km", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Velocidad máx.:", color = TextMuted, fontSize = 12.sp)
                                                Text("${maxSpeedEver.toInt()} km/h", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 2x2 Infraction Pills
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            InfractionPill(
                                                emoji = "🏎️",
                                                count = consolidatedDrives.sumOf { it.events.speeding.size },
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    activeExplanationDialog = ExplanationType.SPEEDING
                                                }
                                            )
                                            InfractionPill(
                                                emoji = "📱",
                                                count = consolidatedDrives.sumOf { it.events.phoneDistractions.size },
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    activeExplanationDialog = ExplanationType.DISTRACTION
                                                }
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            InfractionPill(
                                                emoji = "⚡",
                                                count = consolidatedDrives.sumOf { it.events.rapidAccelerations.size },
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    activeExplanationDialog = ExplanationType.ACCELERATION
                                                }
                                            )
                                            InfractionPill(
                                                emoji = "🛑",
                                                count = consolidatedDrives.sumOf { it.events.hardBrakes.size },
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    activeExplanationDialog = ExplanationType.BRAKING
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Member vertical selector (Breakdown cards, stacked full-width)
                        item {
                            Text(
                                text = "Desglose por Usuario",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(selectedCircle.members) { member ->
                            val isSelected = selectedMember?.id == member.id
                            val memberFilteredDrives = allFilteredDrives[member.id] ?: emptyList()
                            
                            val tripsCount = memberFilteredDrives.size
                            val totalDist = memberFilteredDrives.sumOf { it.distanceKm }
                            val maxSpd = memberFilteredDrives.maxOfOrNull { it.maxSpeed } ?: 0.0
                            val score = if (memberFilteredDrives.isNotEmpty()) {
                                memberFilteredDrives.map { it.safetyScore }.average().toInt()
                            } else {
                                100
                            }

                            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                            val density = androidx.compose.ui.platform.LocalDensity.current
                            val isAdaptiveLayout = configuration.screenWidthDp < 385 || density.fontScale > 1.15f

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedMember = member
                                        viewModel.loadMemberDrives(member.id)
                                    },
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) BorderStroke(2.dp, PrimaryEmerald) else BorderStroke(1.dp, BorderColor)
                            ) {
                                if (isAdaptiveLayout) {
                                    // Layout de dos filas para pantallas pequeñas o letras grandes
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Fila Superior: Avatar + Nombre Completo
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            val memberAvatarUrl = if (!member.avatarUrl.isNullOrEmpty() && member.avatarUrl != "null") {
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

                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(DarkBackground, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                SubcomposeAsyncImage(
                                                    model = memberAvatarUrl,
                                                    contentDescription = member.name,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop,
                                                    loading = {
                                                        Text(initials, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    },
                                                    error = {
                                                        Text(initials, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                )
                                            }

                                            Text(
                                                text = member.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        HorizontalDivider(color = BorderColor, thickness = 1.dp)

                                        // Fila Inferior: Estadísticas distribuidas uniformemente
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Score
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Score", color = TextMuted, fontSize = 10.sp)
                                                Text(
                                                    text = "$score",
                                                    color = when {
                                                        score >= 90 -> PrimaryEmerald
                                                        score >= 70 -> PrimaryOrange
                                                        else -> PrimaryRed
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            // Viajes
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Viajes", color = TextMuted, fontSize = 10.sp)
                                                Text("$tripsCount", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }

                                            // Distancia
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Distancia", color = TextMuted, fontSize = 10.sp)
                                                Text("${String.format(Locale.US, "%.1f", totalDist)} km", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }

                                            // Velocidad Máxima
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Vel. Máx", color = TextMuted, fontSize = 10.sp)
                                                Text("${maxSpd.toInt()} km/h", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                } else {
                                    // Layout compacto original de una fila para pantallas grandes y letra normal
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Left side: Avatar + Name
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.width(110.dp)
                                        ) {
                                            val memberAvatarUrl = if (!member.avatarUrl.isNullOrEmpty() && member.avatarUrl != "null") {
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

                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(DarkBackground, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                SubcomposeAsyncImage(
                                                    model = memberAvatarUrl,
                                                    contentDescription = member.name,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop,
                                                    loading = {
                                                        Text(initials, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    },
                                                    error = {
                                                        Text(initials, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                )
                                            }

                                            Text(
                                                text = member.name.substringBefore(" "),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Divider between Avatar/Name and Stats
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(30.dp)
                                                .background(BorderColor)
                                        )

                                        // Right side: Stats horizontal list
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Score
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Score", color = TextMuted, fontSize = 9.sp)
                                                Text(
                                                    text = "$score",
                                                    color = when {
                                                        score >= 90 -> PrimaryEmerald
                                                        score >= 70 -> PrimaryOrange
                                                        else -> PrimaryRed
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            // Trips
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Viajes", color = TextMuted, fontSize = 9.sp)
                                                Text("$tripsCount", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            // Distance
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Distancia", color = TextMuted, fontSize = 9.sp)
                                                Text("${String.format(Locale.US, "%.1f", totalDist)} km", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            // Max Speed
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Vel. Máx", color = TextMuted, fontSize = 9.sp)
                                                Text("${maxSpd.toInt()} km/h", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Drives List header
                        item {
                            Text(
                                text = if (isPremium) "Trayectos Recientes" else "Último Trayecto (Vista Previa)",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        val drivesToShow = if (isPremium) {
                            filteredDrives
                        } else {
                            if (selectedWeekIndex == 0) filteredDrives.take(1) else emptyList()
                        }

                        if (drivesToShow.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (!isPremium && selectedWeekIndex > 0)
                                            "Historial bloqueado. Pásate a Premium para ver el detalle."
                                            else "No se registran viajes en este período.",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            items(drivesToShow) { drive ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isPremium) {
                                                activeDetailDrive = drive
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = formatDate(drive.startTime),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "${formatTime(drive.startTime)} - ${formatTime(drive.endTime)}",
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                            }

                                            // Score Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = when {
                                                            drive.safetyScore >= 90 -> PrimaryEmerald.copy(alpha = 0.15f)
                                                            drive.safetyScore >= 70 -> PrimaryOrange.copy(alpha = 0.15f)
                                                            else -> PrimaryRed.copy(alpha = 0.15f)
                                                        },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        0.5.dp,
                                                        when {
                                                            drive.safetyScore >= 90 -> PrimaryEmerald
                                                            drive.safetyScore >= 70 -> PrimaryOrange
                                                            else -> PrimaryRed
                                                        },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${drive.safetyScore}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        drive.safetyScore >= 90 -> PrimaryEmerald
                                                        drive.safetyScore >= 70 -> PrimaryOrange
                                                        else -> PrimaryRed
                                                    },
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Distancia", color = TextMuted, fontSize = 10.sp)
                                                Text("${drive.distanceKm} km", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Column {
                                                Text("Duración", color = TextMuted, fontSize = 10.sp)
                                                Text("${drive.durationSeconds / 60} min", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Column {
                                                Text("Vel. Máxima", color = TextMuted, fontSize = 10.sp)
                                                Text("${drive.maxSpeed.toInt()} km/h", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }

                                        if (isPremium) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Ver detalles en el mapa",
                                                    fontSize = 11.sp,
                                                    color = PrimaryEmerald,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = PrimaryEmerald,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Premium paywall banner for free users
                        if (!isPremium) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF2E1A47),
                                                        DarkSurfaceVariant
                                                    )
                                                )
                                            )
                                            .padding(20.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "⭐ Obtener Estoy Ok Premium",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = Color(0xFFFBBF24),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Desbloquea el historial de viajes completo, puntaje de seguridad semanal, mapas interactivos de recorridos e informes de excesos de velocidad, aceleraciones y frenadas bruscas de tu familia.",
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                            Button(
                                                onClick = {
                                                    navController?.navigate(Screen.Premium.route)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PrimaryEmerald,
                                                    contentColor = DarkBackground
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Ver Planes Premium", fontWeight = FontWeight.Bold)
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

    // Infraction details dialog
    if (activeExplanationDialog != ExplanationType.NONE) {
        val (title, explanation) = when (activeExplanationDialog) {
            ExplanationType.SPEEDING -> "Exceso de Velocidad" to "Se registra cuando la velocidad del vehículo supera el límite configurado para el círculo (por defecto 120 km/h) o límites urbanos."
            ExplanationType.DISTRACTION -> "Distracción (Celular)" to "Se registra cuando se detecta el uso o manipulación del teléfono móvil con la pantalla encendida mientras el vehículo está en movimiento."
            ExplanationType.ACCELERATION -> "Aceleración Rápida" to "Se registra cuando el vehículo incrementa su velocidad de forma brusca (12 km/h o más en un lapso de 3 segundos)."
            ExplanationType.BRAKING -> "Frenada Brusca" to "Se registra cuando el vehículo disminuye su velocidad bruscamente (15 km/h o más en un lapso de 3 segundos)."
            else -> "" to ""
        }

        AlertDialog(
            onDismissRequest = { activeExplanationDialog = ExplanationType.NONE },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = explanation,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { activeExplanationDialog = ExplanationType.NONE }) {
                    Text("Entendido", color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Drive Details Dialog
    activeDetailDrive?.let { drive ->
        DriveMapDialog(
            drive = drive,
            onDismiss = { activeDetailDrive = null }
        )
    }
}

@Composable
fun DriveMapDialog(
    drive: MemberDriveEventDto,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            val path = remember(drive) {
                drive.routePoints.map { LatLng(it.latitude, it.longitude) }
            }

            val cameraPositionState = rememberCameraPositionState()

            // Automatically fit bounds
            LaunchedEffect(path) {
                if (path.isNotEmpty()) {
                    val builder = LatLngBounds.builder()
                    path.forEach { builder.include(it) }
                    val bounds = builder.build()
                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true
                )
            ) {
                if (path.isNotEmpty()) {
                    Polyline(
                        points = path,
                        color = PrimaryEmerald,
                        width = 8f
                    )

                    // Start marker (Traffic Light Emoji Badge)
                    MarkerComposable(
                        state = rememberMarkerState(position = path.first()),
                        title = "Inicio del trayecto"
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White, CircleShape)
                                .border(1.5.dp, PrimaryEmerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🚦",
                                fontSize = 18.sp
                            )
                        }
                    }

                    // End marker (Checkered Flag Emoji Badge)
                    MarkerComposable(
                        state = rememberMarkerState(position = path.last()),
                        title = "Fin del trayecto"
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White, CircleShape)
                                .border(1.5.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏁",
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Telemetry events
                    drive.events.hardBrakes.forEach { event ->
                        MarkerComposable(
                            state = rememberMarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = "Frenada Brusca",
                            snippet = "Bajó ${event.speedDrop?.toInt()} km/h"
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(PrimaryOrange, CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    drive.events.rapidAccelerations.forEach { event ->
                        MarkerComposable(
                            state = rememberMarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = "Aceleración Rápida",
                            snippet = "Subió ${event.speedGain?.toInt()} km/h"
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(PrimaryOrange, CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    drive.events.speeding.forEach { event ->
                        MarkerComposable(
                            state = rememberMarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = "Exceso de Velocidad",
                            snippet = "Velocidad: ${event.speed?.toInt()} km/h (Límite: ${event.limit})"
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(PrimaryRed, CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    drive.events.phoneDistractions.forEach { event ->
                        MarkerComposable(
                            state = rememberMarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = "Uso del Teléfono / Distracción",
                            snippet = "Duración: ${event.durationSeconds} s"
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(PrimaryRed, CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Close button (Top Left)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(DarkSurface.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = TextPrimary
                )
            }

            // Bottom Summary Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detalle del Trayecto",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )

                        // Safety score badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when {
                                        drive.safetyScore >= 90 -> PrimaryEmerald.copy(alpha = 0.2f)
                                        drive.safetyScore >= 70 -> PrimaryOrange.copy(alpha = 0.2f)
                                        else -> PrimaryRed.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Score: ${drive.safetyScore}",
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    drive.safetyScore >= 90 -> PrimaryEmerald
                                    drive.safetyScore >= 70 -> PrimaryOrange
                                    else -> PrimaryRed
                                },
                                fontSize = 12.sp
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distancia", color = TextMuted, fontSize = 11.sp)
                            Text("${drive.distanceKm} km", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Duración", color = TextMuted, fontSize = 11.sp)
                            Text("${drive.durationSeconds / 60} min", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Vel. Máxima", color = TextMuted, fontSize = 11.sp)
                            Text("${drive.maxSpeed.toInt()} km/h", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    // Event counters summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (drive.events.hardBrakes.isNotEmpty()) {
                            BadgeItem(text = "🛑 Frenadas: ${drive.events.hardBrakes.size}", color = PrimaryOrange)
                        }
                        if (drive.events.rapidAccelerations.isNotEmpty()) {
                            BadgeItem(text = "⚡ Acel.: ${drive.events.rapidAccelerations.size}", color = PrimaryOrange)
                        }
                        if (drive.events.speeding.isNotEmpty()) {
                            BadgeItem(text = "🏎️ Excesos: ${drive.events.speeding.size}", color = PrimaryRed)
                        }
                        if (drive.events.phoneDistractions.isNotEmpty()) {
                            BadgeItem(text = "📱 Celular: ${drive.events.phoneDistractions.size}", color = PrimaryRed)
                        }
                        if (drive.events.hardBrakes.isEmpty() && 
                            drive.events.rapidAccelerations.isEmpty() && 
                            drive.events.speeding.isEmpty() &&
                            drive.events.phoneDistractions.isEmpty()) {
                            Text("✅ Conducción impecable sin infracciones", color = PrimaryEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun parseIsoToSeconds(isoStr: String): Long {
    val date = parseIsoDate(isoStr) ?: return 0L
    return date.time / 1000L
}

private fun groupAndMergeDrives(drives: List<MemberDriveEventDto>): List<MemberDriveEventDto> {
    if (drives.isEmpty()) return emptyList()
    val sortedDrives = drives.sortedBy { parseIsoToSeconds(it.startTime) }
    val merged = mutableListOf<MemberDriveEventDto>()
    for (drive in sortedDrives) {
        if (merged.isEmpty()) {
            merged.add(drive)
        } else {
            val last = merged.last()
            val lastEnd = parseIsoToSeconds(last.endTime)
            val currentStart = parseIsoToSeconds(drive.startTime)
            val gapSeconds = currentStart - lastEnd
            // Merge consecutive drives if gap is less than 10 minutes (600 seconds)
            if (gapSeconds in 0..600) {
                val mergedPoints = last.routePoints + drive.routePoints
                val mergedHardBrakes = last.events.hardBrakes + drive.events.hardBrakes
                val mergedAccelerations = last.events.rapidAccelerations + drive.events.rapidAccelerations
                val mergedSpeedings = last.events.speeding + drive.events.speeding
                val mergedDistractions = last.events.phoneDistractions + drive.events.phoneDistractions
                
                val newStartTime = last.startTime
                val newEndTime = if (parseIsoToSeconds(drive.endTime) > parseIsoToSeconds(last.endTime)) drive.endTime else last.endTime
                val newDurationSeconds = parseIsoToSeconds(newEndTime) - parseIsoToSeconds(newStartTime)
                val newDistanceKm = last.distanceKm + drive.distanceKm
                val newMaxSpeed = maxOf(last.maxSpeed, drive.maxSpeed)
                val newExceededSpeedLimit = last.exceededSpeedLimit || drive.exceededSpeedLimit
                val newSafetyScore = ((last.safetyScore + drive.safetyScore) / 2)
                
                val updatedLast = last.copy(
                    endTime = newEndTime,
                    durationSeconds = newDurationSeconds,
                    distanceKm = newDistanceKm,
                    maxSpeed = newMaxSpeed,
                    exceededSpeedLimit = newExceededSpeedLimit,
                    safetyScore = newSafetyScore,
                    routePoints = mergedPoints,
                    events = last.events.copy(
                        hardBrakes = mergedHardBrakes,
                        rapidAccelerations = mergedAccelerations,
                        speeding = mergedSpeedings,
                        phoneDistractions = mergedDistractions
                    )
                )
                merged[merged.lastIndex] = updatedLast
            } else {
                merged.add(drive)
            }
        }
    }
    return merged.sortedByDescending { parseIsoToSeconds(it.startTime) }
}

private fun parseIsoDate(isoString: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString)
            if (date != null) return date
        } catch (e: Exception) {
            // Try next pattern
        }
    }
    return null
}

private fun formatTime(isoString: String): String {
    val date = parseIsoDate(isoString) ?: return isoString
    return try {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}

private fun formatDate(isoString: String): String {
    val date = parseIsoDate(isoString) ?: return isoString
    return try {
        val formatter = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}

@Composable
fun InfractionPill(
    emoji: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BorderColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
