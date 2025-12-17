package org.noiseplanet.noisecapture.ui.features.map

import androidx.compose.runtime.Composable
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.map.MapView


@OptIn(KoinExperimentalAPI::class)
@Composable
fun CommunityMapScreen() {

    // - DI

    rememberKoinModules {
        listOf(communityMapModule)
    }


    // - Properties


    // - Layout

    MapView()
}
