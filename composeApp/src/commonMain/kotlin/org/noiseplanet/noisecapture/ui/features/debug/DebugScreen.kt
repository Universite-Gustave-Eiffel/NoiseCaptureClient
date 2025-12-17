package org.noiseplanet.noisecapture.ui.features.debug

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.map.MapView

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DebugScreen(
//    viewModel: DebugScreenViewModel,
) {
    // - DI

    rememberKoinModules {
        listOf(debugModule)
    }


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        MapView(modifier = Modifier.fillMaxSize())
    }
}
