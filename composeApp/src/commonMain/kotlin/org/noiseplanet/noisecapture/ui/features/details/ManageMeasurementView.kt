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
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_audio_size
import noisecapture.composeapp.generated.resources.measurement_details_manage_description
import noisecapture.composeapp.generated.resources.measurement_details_manage_title
import noisecapture.composeapp.generated.resources.measurement_details_total_size
import org.jetbrains.compose.resources.stringResource
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
                    text = stringResource(Res.string.measurement_details_manage_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = stringResource(Res.string.measurement_details_manage_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                state.audioFileSize?.let {
                    Text(
                        text = stringResource(Res.string.measurement_details_audio_size) +
                            HumanReadable.fileSize(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }

                state.measurementSize?.let {
                    Text(
                        text = stringResource(Res.string.measurement_details_total_size) +
                            HumanReadable.fileSize(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }

                state.deleteAudioButtonViewModel?.let { buttonViewModel ->
                    NCButton(
                        viewModel = buttonViewModel,
                        onClick = viewModel::deleteMeasurementAudio,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 48.dp)
                            .padding(top = 8.dp),
                    )
                }

                NCButton(
                    viewModel = state.deleteMeasurementButtonViewModel,
                    onClick = viewModel::deleteMeasurement,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 48.dp)
                )
            }
        }

        else -> return
    }
}
