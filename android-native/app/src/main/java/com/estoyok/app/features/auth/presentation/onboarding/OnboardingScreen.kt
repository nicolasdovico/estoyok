package com.estoyok.app.features.auth.presentation.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estoyok.app.R
import com.estoyok.app.core.theme.*
import kotlinx.coroutines.launch

data class OnboardingStep(
    val tagText: String,
    val tagColor: Color,
    val title: String,
    val description: String,
    val imageRes: Int,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OnboardingStep(
            tagText = "🟢 MAPA EN TIEMPO REAL",
            tagColor = PrimaryEmerald,
            title = "Tu Familia Conectada\nen Todo Momento",
            description = "Localizá a tu núcleo en un mapa interactivo con nivel de batería, estado de señal y actualización continua.",
            imageRes = R.drawable.img_onboarding_01_mapa,
            accentColor = PrimaryEmerald
        ),
        OnboardingStep(
            tagText = "🛡️ CHECK-IN DE BIENESTAR",
            tagColor = PrimaryEmerald,
            title = "Confirmá tu Bienestar\nen un Solo Toque",
            description = "Un toque diario para confirmar que estás a salvo. Si no lo hacés en tu horario, la app le avisa automáticamente a tus contactos por WhatsApp.",
            imageRes = R.drawable.img_onboarding_02_estoy_ok,
            accentColor = PrimaryEmerald
        ),
        OnboardingStep(
            tagText = "📍 LLEGADAS Y SALIDAS AUTOMÁTICAS",
            tagColor = PrimaryOrange,
            title = "Avisos de Casa,\nEscuela y Trabajo",
            description = "Configurá perímetros frecuentes y recibí alertas automáticas instantáneas al entrar o salir, sin necesidad de que te escriban 'ya llegué'.",
            imageRes = R.drawable.img_onboarding_03_zonas_seguras,
            accentColor = PrimaryOrange
        ),
        OnboardingStep(
            tagText = "🧭 HISTORIAL DE MOVIMIENTOS",
            tagColor = Color(0xFF38BDF8),
            title = "Rutas y Trayectos\ndel Día a Día",
            description = "Consultá los recorridos realizados sobre el mapa calle por calle, con horarios de salida, llegada y duración de cada viaje.",
            imageRes = R.drawable.img_onboarding_04_rutas,
            accentColor = Color(0xFF38BDF8)
        ),
        OnboardingStep(
            tagText = "🏠 CONFIRMACIÓN POR WI-FI",
            tagColor = PrimaryEmerald,
            title = "Auto Check-in Cómodo\nen tu Casa",
            description = "Vinculá el Wi-Fi de tu casa para reportar tu bienestar automáticamente sin necesidad de abrir la app todos los días.",
            imageRes = R.drawable.img_onboarding_05_hogar,
            accentColor = PrimaryEmerald
        ),
        OnboardingStep(
            tagText = "🚗 PROTECCIÓN VEHICULAR",
            tagColor = PrimaryOrange,
            title = "Hábitos de Manejo\ny Velocidad",
            description = "Monitoreá viajes familiares en auto con puntuación vial, detección de excesos de velocidad, frenadas bruscas y uso del celular al volante.",
            imageRes = R.drawable.img_onboarding_06_conduccion,
            accentColor = PrimaryOrange
        ),
        OnboardingStep(
            tagText = "🚨 ASISTENCIA ANTE IMPACTOS",
            tagColor = Color(0xFFEF4444),
            title = "Respuesta Inmediata\nante Accidentes",
            description = "Detección inteligente de colisiones con sensores de Fuerza G y cuenta regresiva para cancelar antes de disparar el auxilio a tus seres queridos.",
            imageRes = R.drawable.img_onboarding_07_choque,
            accentColor = Color(0xFFEF4444)
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
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Counter and Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} de ${steps.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (!isLastPage) {
                    TextButton(
                        onClick = onFinish,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = TextMuted
                        )
                    ) {
                        Text(
                            text = "Omitir",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
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
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Modern Floating Phone Mockup with Real App Screenshot
                    OnboardingPhoneMockup(
                        imageRes = step.imageRes,
                        accentColor = step.accentColor,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(min = 210.dp, max = 290.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Badge / Category Tag
                    Surface(
                        color = step.tagColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, step.tagColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = step.tagText,
                            color = step.tagColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            letterSpacing = 0.6.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = step.title,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 27.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = step.description,
                        color = TextSecondary,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Indicators (Dots)
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(steps.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
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
                    .height(52.dp)
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

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun OnboardingPhoneMockup(
    imageRes: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow behind phone
        Box(
            modifier = Modifier
                .fillMaxHeight(0.92f)
                .aspectRatio(9f / 19.5f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
        )

        // Phone Frame
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(9f / 20f)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = accentColor.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF070B16))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.7f),
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Real App Screenshot inside
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(17.dp))
            )

            // Front camera punch-hole notch
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B101E))
            )
        }
    }
}
