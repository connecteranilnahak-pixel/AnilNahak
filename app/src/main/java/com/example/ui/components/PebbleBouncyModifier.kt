package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

/**
 * Pebble Time 2 Bouncy Spring Press Modifier:
 * Scales down to 0.95f on pointer press and springs back with bouncy damping on release.
 * Triggers tactile haptic feedback.
 */
fun Modifier.pebbleBouncyClickable(
    hapticFeedback: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val localHaptics = LocalHapticFeedback.current

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    if (hapticFeedback) {
                        localHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    scope.launch {
                        scale.animateTo(
                            0.95f,
                            spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                    val released = tryAwaitRelease()
                    scope.launch {
                        scale.animateTo(
                            1f,
                            spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                    if (released) {
                        onClick()
                    }
                }
            )
        }
}
