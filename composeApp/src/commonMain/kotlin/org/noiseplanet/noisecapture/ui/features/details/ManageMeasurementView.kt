package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.delete
import noisecapture.composeapp.generated.resources.measurement_details_audio_size
import noisecapture.composeapp.generated.resources.measurement_details_delete_measurement_audio_dialog_text
import noisecapture.composeapp.generated.resources.measurement_details_delete_measurement_dialog_text
import noisecapture.composeapp.generated.resources.measurement_details_delete_measurement_dialog_title
import noisecapture.composeapp.generated.resources.measurement_details_manage_description
import noisecapture.composeapp.generated.resources.measurement_details_manage_title
import noisecapture.composeapp.generated.resources.measurement_details_total_size
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


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

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember {
        mutableStateOf(Res.string.measurement_details_delete_measurement_dialog_text)
    }
    var deleteConfirmationAction: (() -> Unit)? by remember {
        mutableStateOf(null)
    }


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

                Spacer(modifier = Modifier.height(4.dp))

                state.deleteAudioButtonViewModel?.let { buttonViewModel ->
                    NCButton(
                        viewModel = buttonViewModel,
                        onClick = {
                            deleteConfirmationText =
                                Res.string.measurement_details_delete_measurement_audio_dialog_text
                            deleteConfirmationAction = viewModel::deleteMeasurementAudio
                            showDeleteConfirmationDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 48.dp)
                    )
                }

                NCButton(
                    viewModel = state.deleteMeasurementButtonViewModel,
                    onClick = {
                        deleteConfirmationText =
                            Res.string.measurement_details_delete_measurement_dialog_text
                        deleteConfirmationAction = viewModel::deleteMeasurement
                        showDeleteConfirmationDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 48.dp)
                )
            }
        }

        else -> return
    }

    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            title = Res.string.measurement_details_delete_measurement_dialog_title,
            text = deleteConfirmationText,
            onDismissRequest = { showDeleteConfirmationDialog = false },
            onConfirm = {
                deleteConfirmationAction?.let { it() }
                showDeleteConfirmationDialog = false
            }
        )
    }
}


@Composable
private fun DeleteConfirmationDialog(
    title: StringResource,
    text: StringResource,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    // - Properties

    val confirmButtonViewModel = NCButtonViewModel(
        title = Res.string.delete,
        style = NCButtonStyle.TEXT,
        colors = {
            NCButtonColors.Defaults.text()
                .copy(contentColor = MaterialTheme.colorScheme.error)
        },
    )
    val cancelButtonViewModel = NCButtonViewModel(
        title = Res.string.cancel,
        style = NCButtonStyle.TEXT,
        colors = {
            NCButtonColors.Defaults.text()
        },
    )


    // - Layout

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            NCButton(onClick = onConfirm, viewModel = confirmButtonViewModel)
        },
        dismissButton = {
            NCButton(onClick = onDismissRequest, viewModel = cancelButtonViewModel)
        },
        title = {
            Text(stringResource(title))
        },
        text = {
            Text(stringResource(text))
        },
    )
}
