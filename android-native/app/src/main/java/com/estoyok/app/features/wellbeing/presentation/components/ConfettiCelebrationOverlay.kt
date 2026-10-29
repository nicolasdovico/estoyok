package com.estoyok.app.features.wellbeing.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val initialXRatio: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val swayFrequency: Float
)

@Composable
fun ConfettiCelebrationOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 90
) {
    val particles = remember {
        val vibrantColors = listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF6366F1), // Indigo
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899), // Pink
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6)  // Purple
        )
        List(particleCount) {
            ConfettiParticle(
                initialXRatio = Random.nextFloat(),
                velocityX = Random.nextFloat() * 120f - 60f,
                velocityY = Random.nextFloat() * 450f + 350f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                color = vibrantColors[Random.nextInt(vibrantColors.size)],
                width = Random.nextFloat() * 12f + 8f,
                height = Random.nextFloat() * 20f + 12f,
                swayFrequency = Random.nextFloat() * 4f + 2f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val startX = p.initialXRatio * canvasWidth
            val currentY = -40f + (p.velocityY * time)
            val currentX = startX + (p.velocityX * time) + (sin(time * p.swayFrequency) * 40f)

            if (currentY in -50f..canvasHeight + 50f) {
                rotate(
                    degrees = p.rotationSpeed * time,
                    pivot = Offset(currentX, currentY)
                ) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(currentX - p.width / 2f, currentY - p.height / 2f),
                        size = Size(p.width, p.height)
                    )
                }
            }
        }
    }
}
