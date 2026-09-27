package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutSegment(
    val id: String,
    val value: Double,
    val color: Color,
    val label: String = ""
)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp,
    gapDegree: Float = 3f,
    emptyTrackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    centerContent: @Composable () -> Unit = {}
) {
    val total = segments.sumOf { it.value }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(segments) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - strokePx
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Draw background track ring
            drawArc(
                color = emptyTrackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (total > 0 && segments.isNotEmpty()) {
                val nonZeroSegments = segments.filter { it.value > 0 }
                val numGaps = if (nonZeroSegments.size > 1) nonZeroSegments.size else 0
                val totalAvailableDegrees = 360f - (numGaps * gapDegree)
                var currentAngle = -90f

                for (segment in nonZeroSegments) {
                    val rawSweep = (segment.value / total).toFloat() * totalAvailableDegrees
                    val sweep = (rawSweep * animationProgress.value).coerceAtLeast(0.5f)

                    drawArc(
                        color = segment.color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    currentAngle += rawSweep + gapDegree
                }
            }
        }

        Box(
            modifier = Modifier.padding(strokeWidth + 8.dp),
            contentAlignment = Alignment.Center
        ) {
            centerContent()
        }
    }
}
