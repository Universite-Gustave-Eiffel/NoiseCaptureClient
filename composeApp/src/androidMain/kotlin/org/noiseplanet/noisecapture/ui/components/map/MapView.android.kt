package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sargunv.maplibrecompose.compose.MaplibreMap


@Composable
actual fun MapView(modifier: Modifier) {

    MaplibreMap(modifier.fillMaxSize())
}
