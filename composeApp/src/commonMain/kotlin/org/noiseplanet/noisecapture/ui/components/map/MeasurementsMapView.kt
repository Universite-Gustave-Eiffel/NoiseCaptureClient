package org.noiseplanet.noisecapture.ui.components.map

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
        modifier = Modifier,
        state = viewModel.mapState,
    )
}
