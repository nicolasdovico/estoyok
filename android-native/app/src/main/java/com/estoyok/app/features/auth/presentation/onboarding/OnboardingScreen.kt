package com.estoyok.app.features.auth.presentation.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estoyok.app.core.theme.*
import kotlinx.coroutines.launch

data class OnboardingStep(
    val tagText: String,
    val tagColor: Color,
    val title: String,
    val description: String,
    val primaryIcon: ImageVector,
    val visualType: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OnboardingStep(
            tagText = "🟢 LOCALIZACIÓN EN TIEMPO REAL",
            tagColor = PrimaryEmerald,
            title = "Tu Familia Conectada\nen Todo Momento",
            description = "Creá tu Núcleo Familiar con un simple código de 6 dígitos. Podrás ver dónde están tus seres queridos en un mapa interactivo con deslizamiento continuo y en tiempo real.",
            primaryIcon = Icons.Default.People,
            visualType = 1
        ),
        OnboardingStep(
            tagText = "🛡️ CUIDADO DIARIO AUTOMÁTICO",
            tagColor = PrimaryEmerald,
            title = "Confirmá tu Bienestar\nen un Solo Toque",
            description = "Presioná el botón al iniciar tu día para confirmar que estás bien. Si no lo hacés en tu horario habitual, la app le enviará una alerta automática inmediata a tus contactos de emergencia por WhatsApp.",
            primaryIcon = Icons.Default.Shield,
            visualType = 2
        ),
        OnboardingStep(
            tagText = "📍 LLEGADAS Y SALIDAS AUTOMÁTICAS",
            tagColor = PrimaryOrange,
            title = "Avisos de Casa,\nEscuela y Trabajo",
            description = "Configurá lugares frecuentes y recibí notificaciones automáticas instantáneas cuando tus familiares lleguen o salgan de su destino, sin necesidad de que te escriban 'ya llegué'.",
            primaryIcon = Icons.Default.LocationOn,
            visualType = 3
        )
    )

    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == steps.size - 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    TextButton(
                        onClick = onFinish,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = TextMuted
                        )
                    ) {
                        Text(
                            text = "Omitir",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Pager for Onboarding Steps
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val step = steps[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Visual Graphic Card
                    OnboardingVisualGraphic(visualType = step.visualType)

                    Spacer(modifier = Modifier.height(36.dp))

                    // Badge / Category Tag
                    Surface(
                        color = step.tagColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, step.tagColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = step.tagText,
                            color = step.tagColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Title
                    Text(
                        text = step.title,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description
                    Text(
                        text = step.description,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicators (Dots)
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(steps.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 26.dp else 8.dp,
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) PrimaryEmerald else TextMuted.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            // Bottom Action Button
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(bottom = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryEmerald,
                    contentColor = TextOnPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (isLastPage) "¡Comenzar a usar Estoy Ok! 🚀" else "Siguiente →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OnboardingVisualGraphic(visualType: Int) {
    Box(
        modifier = Modifier
            .size(210.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DarkSurfaceVariant,
                        CardBackground
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        BorderColor.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (visualType) {
            1 -> GraphicFamilyNetwork()
            2 -> GraphicEstoyOkButton()
            3 -> GraphicSafeZones()
        }
    }
}

/**
 * Graphic 1: Family Network & Map Pin Visualization
 */
@Composable
private fun GraphicFamilyNetwork() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing connection ring
        Box(
            modifier = Modifier
                .size(150.dp)
                .border(1.5.dp, PrimaryEmerald.copy(alpha = 0.25f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(105.dp)
                .border(1.5.dp, PrimaryEmerald.copy(alpha = 0.45f), CircleShape)
        )

        // Central Shield & Location Hub
        Surface(
            modifier = Modifier.size(62.dp),
            shape = CircleShape,
            color = PrimaryEmerald,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextOnPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Surrounding Family Member Avatars
        MemberBadge(
            text = "Mamá",
            color = PrimaryTeal,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 22.dp, top = 22.dp)
        )
        MemberBadge(
            text = "Hijo",
            color = PrimaryOrange,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp)
        )
        MemberBadge(
            text = "Papá",
            color = Color(0xFF3B82F6),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun MemberBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color,
        border = androidx.compose.foundation.BorderStroke(2.dp, DarkSurface),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.take(1),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Graphic 2: Daily Check-In "Estoy OK" Glowing Button
 */
@Composable
private fun GraphicEstoyOkButton() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow ring
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(CircleShape)
                .background(PrimaryEmerald.copy(alpha = 0.15f))
        )
        // Secondary ring
        Box(
            modifier = Modifier
                .size(115.dp)
                .border(2.dp, PrimaryEmerald.copy(alpha = 0.35f), CircleShape)
        )

        // Main circular button
        Surface(
            modifier = Modifier.size(92.dp),
            shape = CircleShape,
            color = PrimaryEmerald,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = TextOnPrimary,
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = "ESTOY OK",
                    color = TextOnPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Floating WhatsApp Alert Callout
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.4f)),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "📲", fontSize = 12.sp)
                Text(
                    text = "Aviso a WhatsApp",
                    color = PrimaryEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Graphic 3: Safe Zones (Casa / Escuela / Trabajo)
 */
@Composable
private fun GraphicSafeZones() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Safe perimeter radius ring
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(PrimaryEmerald.copy(alpha = 0.12f))
                .border(1.5.dp, PrimaryEmerald.copy(alpha = 0.5f), CircleShape)
        )

        // Center Place Icon (Home / Safe Zone)
        Surface(
            modifier = Modifier.size(66.dp),
            shape = CircleShape,
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryEmerald),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = PrimaryEmerald,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Notification Arrival Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            shape = RoundedCornerShape(14.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.5f)),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "📍", fontSize = 11.sp)
                Text(
                    text = "Llegó a Casa • 2 min",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
