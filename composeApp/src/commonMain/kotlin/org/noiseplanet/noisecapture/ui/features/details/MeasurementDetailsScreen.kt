package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign


@Composable
fun MeasurementDetailsScreen(
    viewModel: MeasurementDetailsScreenViewModel,
) {

    // - Properties

    val measurementState by viewModel.measurementFlow.collectAsState(null)
    val measurement = measurementState ?: return


    // - Layout

    Text(
        text = "Measurement id: ${measurement.uuid}",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxSize(),
    )
}
