package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.button.NCButton


@Composable
fun ManageMeasurementView(
    measurementId: String,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val viewModel: ManageMeasurementViewModel = koinViewModel {
        parametersOf(measurementId)
    }
    val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()


    // - Layout

    when (viewState) {
        is ManageMeasurementViewModel.ViewState.ContentReady -> {
            val state = viewState as ManageMeasurementViewModel.ViewState.ContentReady

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.fillMaxWidth()
            ) {
                state.deleteAudioButtonViewModel?.let { buttonViewModel ->
                    NCButton(
                        viewModel = buttonViewModel,
                        onClick = viewModel::deleteMeasurementAudio
                    )
                }

                NCButton(
                    viewModel = state.deleteMeasurementButtonViewModel,
                    onClick = viewModel::deleteMeasurement
                )
            }
        }

        else -> return
    }
}
