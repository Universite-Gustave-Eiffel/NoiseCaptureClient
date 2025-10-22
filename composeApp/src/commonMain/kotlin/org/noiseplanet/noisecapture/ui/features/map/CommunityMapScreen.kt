package org.noiseplanet.noisecapture.ui.features.map

import androidx.compose.runtime.Composable
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapView


@OptIn(KoinExperimentalAPI::class)
@Composable
fun CommunityMapScreen(
    viewModel: CommunityMapScreenViewModel,
) {

    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(communityMapModule)
    }


    // - Properties


    // - Layout

    MeasurementsMapView()
}
