package org.noiseplanet.noisecapture.util.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

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
fun Modifier.dropShadow(
    shape: Shape,
    color: Color = Color.Black.copy(0.10f),
    blur: Float = 4f,
    offset: Offset = Offset(x = 0f, y = 4f),
    spread: Float = 0f,
) = this.drawBehind {

    val shadowSize = Size(size.width + spread, size.height + spread)
    val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)

    val paint = Paint()
    paint.color = color

    if (blur > 0f) {
        paint.asFrameworkPaint().setBlurMaskFilter(blur)
    }

    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(offset.x, offset.y)
        canvas.drawOutline(shadowOutline, paint)
        canvas.restore()
    }
}
