package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import ovh.plrapps.mapcompose.ui.MapUI


@Composable
fun MeasurementsMapView(modifier: Modifier = Modifier) {

    // - Properties

    val viewModel: MeasurementsMapViewModel = koinViewModel()
    val mapOrientation by viewModel.mapOrientationFlow.collectAsStateWithLifecycle()


    // - Layout

    MapUI(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        state = viewModel.mapState,
    )

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier.fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(modifier = Modifier.fillMaxHeight()) {

            // TODO: Improve compass button styling
            NCButton(
                viewModel = viewModel.compassButtonViewModel,
                onClick = {
                    viewModel.resetOrientation()
                },
                modifier = Modifier.size(48.dp)
                    .rotate(mapOrientation)
            )

            Spacer(modifier = Modifier.weight(1f))

            NCButton(
                viewModel = viewModel.recenterButtonViewModel,
                onClick = {
                    viewModel.recenter()
                    viewModel.autoRecenterEnabled = true
                },
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
