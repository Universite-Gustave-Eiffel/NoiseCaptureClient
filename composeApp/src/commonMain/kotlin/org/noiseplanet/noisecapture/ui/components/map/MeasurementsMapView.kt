package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import ovh.plrapps.mapcompose.ui.MapUI


@Composable
fun MeasurementsMapView(modifier: Modifier = Modifier) {

    // - Properties

    val viewModel: MeasurementsMapViewModel = koinViewModel()


    // - Layout

    MapUI(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        state = viewModel.mapState,
    )
}
