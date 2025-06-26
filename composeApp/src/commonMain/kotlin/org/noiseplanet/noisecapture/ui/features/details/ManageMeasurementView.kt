package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_delete_measurement_button
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


class ManageMeasurementViewModel(
    val measurement: Measurement,
) : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()

    val deleteMeasurementButtonViewModel = NCButtonViewModel(
        title = Res.string.measurement_details_delete_measurement_button,
        colors = {
            NCButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        }
    )


    // - Public functions

    fun deleteMeasurement() {
        viewModelScope.launch {
            measurementService.deleteMeasurement(measurement.uuid)
        }
    }
}


@Composable
fun ManageMeasurementView(
    measurement: Measurement,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val viewModel: ManageMeasurementViewModel = koinViewModel {
        parametersOf(measurement)
    }


    // - Layout

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        NCButton(
            viewModel = viewModel.deleteMeasurementButtonViewModel,
            onClick = viewModel::deleteMeasurement
        )
    }
}
