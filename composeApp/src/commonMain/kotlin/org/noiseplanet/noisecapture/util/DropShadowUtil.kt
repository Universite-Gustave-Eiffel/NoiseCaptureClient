package org.noiseplanet.noisecapture.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import org.noiseplanet.noisecapture.ui.theme.OnSurface

/**
 * Adds a drop shadow effect to the composable.
 *
 * This modifier allows you to draw a shadow behind the composable with various customization options.
 *
 * @param shape The shape of the shadow.
 * @param color The color of the shadow.
 * @param blur The blur radius of the shadow
 * @param offset The shadow offset along the X and Y axes.
 * @param spread The amount to increase the size of the shadow.
 *
 * @return A new `Modifier` with the drop shadow effect applied.
 */
@Composable
fun Modifier.ncDropShadow(
    shape: Shape,
    color: Color = Color.OnSurface,
    alpha: Float = 0.2f,
    blur: Float = 16f,
    offset: Offset = Offset(x = 0f, y = 4f),
    spread: Float = 4f,
    isPressed: Boolean = false,
): Modifier {

    // - Animated properties

    val animatedOffset by animateOffsetAsState(
        targetValue = if (isPressed) {
            Offset.Zero
        } else {
            offset
        }
    )
    val animatedBlur by animateFloatAsState(
        targetValue = if (isPressed) 0f else blur
    )


    // - Draw

    return this.dropShadow(shape) {
        this@dropShadow.color = color
        this@dropShadow.alpha = alpha
        this@dropShadow.offset = animatedOffset
        this@dropShadow.radius = animatedBlur
        this@dropShadow.spread = spread
    }
}
