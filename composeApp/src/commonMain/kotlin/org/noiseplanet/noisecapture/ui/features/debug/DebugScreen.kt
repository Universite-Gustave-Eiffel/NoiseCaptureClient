package org.noiseplanet.noisecapture.ui.features.debug

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.noiseplanet.noisecapture.ui.components.map.MapView


@Composable
fun DebugScreen() {

    MapView(modifier = Modifier.fillMaxSize())
}
