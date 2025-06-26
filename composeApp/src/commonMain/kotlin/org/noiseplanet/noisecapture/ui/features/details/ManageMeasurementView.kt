package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.jacobras.humanreadable.HumanReadable
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
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Manage measurement",
                    style = MaterialTheme.typography.titleMedium,
                )

                viewModel.getAudioFileSize()?.let {
                    Text(
                        text = "Audio file size: " + HumanReadable.fileSize(it)
                    )
                }

                state.deleteAudioButtonViewModel?.let { buttonViewModel ->
                    NCButton(
                        viewModel = buttonViewModel,
                        onClick = viewModel::deleteMeasurementAudio,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                NCButton(
                    viewModel = state.deleteMeasurementButtonViewModel,
                    onClick = viewModel::deleteMeasurement,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
                )
            }
        }

        else -> return
    }
}
