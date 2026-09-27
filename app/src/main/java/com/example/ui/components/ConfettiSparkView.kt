package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class SparkParticle(
    val angle: Double,
    val distance: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun ConfettiSparkEffect(
    isTriggered: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    onAnimationEnd: () -> Unit = {}
) {
    if (!isTriggered) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(
            Color(0xFFE11D48), // Coral
            Color(0xFF0284C7), // Sky Blue
            Color(0xFF059669), // Matcha
            Color(0xFFEA580C), // Peach
            Color(0xFF6366F1), // Lavender
            Color(0xFF9333EA)  // Lilac
        )
        List(14) { index ->
            val angle = (index * (360.0 / 14.0) + Random.nextDouble(-10.0, 10.0)) * (Math.PI / 180.0)
            SparkParticle(
                angle = angle,
                distance = Random.nextFloat() * 40f + 30f,
                color = colors[index % colors.size],
                radius = Random.nextFloat() * 3f + 3f
            )
        }
    }

    LaunchedEffect(isTriggered) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)

        for (particle in particles) {
            val currentDist = particle.distance * p
            val x = center.x + (cos(particle.angle) * currentDist).toFloat()
            val y = center.y + (sin(particle.angle) * currentDist).toFloat()
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.radius * (1f - (p * 0.5f)),
                center = Offset(x, y)
            )
        }
    }
}
