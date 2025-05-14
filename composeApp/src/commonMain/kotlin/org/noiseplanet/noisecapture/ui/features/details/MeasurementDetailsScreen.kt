package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign


@Composable
fun MeasurementDetailsScreen(
    viewModel: MeasurementDetailsScreenViewModel,
    onMeasurementDeleted: () -> Unit,
) {

    // - Properties

    val measurementState by viewModel.measurementFlow.collectAsState(null)
    val measurement = measurementState ?: return


    // - Layout

    Column(
        modifier = Modifier.fillMaxSize()
            .safeContentPadding()
    ) {
        Text(
            text = "Measurement id: ${measurement.uuid}",
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.deleteMeasurement()
                onMeasurementDeleted()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete")
        }
    }
}
