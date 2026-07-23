package org.noiseplanet.noisecapture.ui.components

import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.theme.Noise
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OptionalSlider(
    value: Float?,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = Color.Noise.two.dark,
        activeTrackColor = Color.Noise.two.light,
        activeTickColor = Color.Noise.two.dark,
        inactiveTrackColor = Color.Noise.two.light,
        inactiveTickColor = Color.Noise.two.dark,
    ),
    modifier: Modifier = Modifier,
) {
    // - Properties

    val interactionSource = remember { MutableInteractionSource() }


    // - Layout

    Slider(
        value = value ?: 0f,
        onValueChange = onValueChange,
        valueRange = valueRange,
        track = { sliderState ->
            val value = if (value == null) null else sliderState.value
            TickedSliderTrack(value, colors = colors, modifier = Modifier.height(24.dp))
        },
        thumb = { sliderState ->
            if (value != null) {
                SliderDefaults.Thumb(
                    sliderState = sliderState,
                    interactionSource = interactionSource,
                    colors = colors,
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}


@Composable
private fun TickedSliderTrack(
    value: Float?,
    @IntRange(from = 2) ticksCount: Int = 5,
    colors: SliderColors = SliderDefaults.colors(),
    modifier: Modifier = Modifier,
) {
    // - Properties

    // Size of the gap between active and inactive track (2 * 6dp gap + 4dp thumb width)
    val gap = (6 * 2 + 4).dp


    // - Layout

    Canvas(modifier = modifier.fillMaxWidth()) {
        // Calculate corner radii and placement of active and inactive tracks
        val tracksPath = Path()
        val outCornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        val inCornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

        if (value != null) {
            val thumbCenter = size.width * value
            val activeTrackEnd = max(thumbCenter - gap.toPx() / 2f, 0f)
            val inactiveTrackStart = min(thumbCenter + gap.toPx() / 2f, size.width)

            // Create rounded rects for each track
            val activeTrackRect = RoundRect(
                rect = Rect(
                    offset = Offset.Zero,
                    size = Size(width = activeTrackEnd, height = 24.dp.toPx())
                ),
                topLeft = outCornerRadius,
                topRight = inCornerRadius,
                bottomLeft = outCornerRadius,
                bottomRight = inCornerRadius,
            )
            val inactiveTrackRect = RoundRect(
                rect = Rect(
                    offset = Offset(inactiveTrackStart, 0f),
                    size = Size(width = size.width - inactiveTrackStart, height = 24.dp.toPx())
                ),
                topLeft = inCornerRadius,
                topRight = outCornerRadius,
                bottomLeft = inCornerRadius,
                bottomRight = outCornerRadius,
            )
            // Add both tracks to a single Path element (used to clip ticks later on)
            tracksPath.addRoundRect(activeTrackRect)
            tracksPath.addRoundRect(inactiveTrackRect)
        } else {
            // If value is null, draw a single track that spans the whole width of the view
            val trackRect = RoundRect(
                rect = Rect(offset = Offset.Zero, size = size),
                topLeft = outCornerRadius,
                topRight = outCornerRadius,
                bottomLeft = outCornerRadius,
                bottomRight = outCornerRadius,
            )
            tracksPath.addRoundRect(trackRect)
        }

        // Draw tracks to canvas
        drawPath(tracksPath, color = colors.activeTrackColor)

        // Ticks constants
        val ticksPadding = 4.dp.toPx()
        val tickRadius = 2.dp.toPx()
        val ticksPath = Path()

        for (tickIndex in 0..<ticksCount) {
            val tickCenter = Offset(
                x = ticksPadding + tickRadius +
                    tickIndex.toFloat() / (ticksCount - 1) * (size.width - ticksPadding * 2 - tickRadius * 2),
                y = size.height / 2f
            )
            val tickRect = RoundRect(
                rect = Rect(center = tickCenter, radius = tickRadius),
                topLeft = CornerRadius(tickRadius),
                topRight = CornerRadius(tickRadius),
                bottomLeft = CornerRadius(tickRadius),
                bottomRight = CornerRadius(tickRadius),
            )
            // Append each tick to the tick path
            ticksPath.addRoundRect(tickRect)
        }

        val clippedTicksPath = Path.combine(
            operation = PathOperation.Intersect,
            path1 = tracksPath,
            path2 = ticksPath
        )
        // Draw ticks
        drawPath(clippedTicksPath, color = colors.activeTickColor)
    }
}
