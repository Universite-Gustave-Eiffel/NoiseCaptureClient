package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp


/**
 * Blue dot showing user's current location.
 *
 * MapCompose places markers based on the bottom center of the view (so that a pin marker pins
 * the exact location it is placed above).
 *
 * @param orientationDegrees Direction in which the user is facing (in decimal degrees).
 *                           Clockwise rotation using north as 0 degrees.
 * @param mapRotationDegrees Direction in which the map is facing (in decimal degrees).
 *                           Clockwise rotation using north as 0 degrees.
 */
@Composable
fun UserLocationMarker(
    mapRotationDegrees: Float,
    orientationDegrees: Float? = null,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val markerColor = Color(0xFF4433FF)


    // - Layout

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.graphicsLayer {
            transformOrigin = TransformOrigin(0.5f, 1f) // Set rotation anchor to bottom center
            rotationZ = mapRotationDegrees
        }
    ) {
        orientationDegrees?.let { _ ->
            // Draw heading gradient arc to show which direction user is looking at
            Box(
                modifier = Modifier.size(128.dp)
                    .drawWithCache {
                        val orientationArcBrush = Brush.radialGradient(
                            colors = listOf(markerColor, markerColor.copy(alpha = 0.0f)),
                            radius = size.height / 2f,
                            center = Offset(size.width / 2f, size.height)
                        )
                        onDrawBehind {
                            drawArc(
                                brush = orientationArcBrush,
                                topLeft = Offset(0f, size.height / 2f),
                                startAngle = -90f - 30f,
                                sweepAngle = 60f,
                                alpha = 0.25f,
                                useCenter = true,
                                size = Size(size.width, size.height),
                            )
                        }
                    }
            )
        }

        // Draw blue dot showing user location
        Box(
            modifier = Modifier.size(24.dp)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = 8.dp,
                        color = markerColor,
                        spread = 12.dp,
                        alpha = 0.1f,
                    )
                )
                .border(
                    width = 4.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(markerColor)
        )
    }
}
