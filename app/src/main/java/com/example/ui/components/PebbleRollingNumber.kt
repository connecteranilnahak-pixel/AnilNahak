package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Pebble Time 2 Mechanical Rolling Numbers (Odometer Effect):
 * Animates vertically using AnimatedContent with slideInVertically { it } togetherWith slideOutVertically { -it }
 * with a spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow).
 */
@Composable
fun PebbleRollingNumber(
    valueText: String,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    textStyle: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                style = textStyle,
                color = color
            )
        }

        // Mechanical per-character odometer roll
        for (i in valueText.indices) {
            val char = valueText[i]
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = spring(
                            dampingRatio = 0.75f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { height -> height }) togetherWith
                    slideOutVertically(
                        animationSpec = spring(
                            dampingRatio = 0.75f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { height -> -height }
                },
                label = "MechanicalOdometerChar_$i"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = textStyle,
                    color = color
                )
            }
        }

        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                style = textStyle,
                color = color
            )
        }
    }
}
