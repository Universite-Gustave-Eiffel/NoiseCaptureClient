package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp


@Composable
fun UserLocationMarker(modifier: Modifier = Modifier) {

    // - Properties

    val markerColor = Color(0xFF4433FF)


    // - Layout

    // TODO: Show orientation

    Box(
        modifier = modifier.size(18.dp)
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
