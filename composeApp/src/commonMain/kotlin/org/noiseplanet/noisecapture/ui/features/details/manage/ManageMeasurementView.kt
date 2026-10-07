package org.noiseplanet.noisecapture.ui.features.details.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.delete
import noisecapture.composeapp.generated.resources.details_audio_size
import noisecapture.composeapp.generated.resources.details_delete_button
import noisecapture.composeapp.generated.resources.details_delete_measurement_dialog_title
import noisecapture.composeapp.generated.resources.details_export_button
import noisecapture.composeapp.generated.resources.details_manage_description
import noisecapture.composeapp.generated.resources.details_manage_title
import noisecapture.composeapp.generated.resources.details_total_size
import noisecapture.composeapp.generated.resources.download
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.NCDialog
import org.noiseplanet.noisecapture.ui.components.NCDropdownMenu
import org.noiseplanet.noisecapture.ui.components.NCDropdownMenuItem
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise


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

    val deleteConfirmDialogState by viewModel.deleteConfirmDialogState
        .collectAsStateWithLifecycle()

    var showExportMenu by remember { mutableStateOf(false) }
    var showDeleteMenu by remember { mutableStateOf(false) }


    // - Layout

    when (viewState) {
        is ManageMeasurementViewModel.ViewState.ContentReady -> {
            val state = viewState as ManageMeasurementViewModel.ViewState.ContentReady

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.details_manage_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = stringResource(Res.string.details_manage_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                state.audioFileSize?.let {
                    Row {
                        Text(
                            text = stringResource(Res.string.details_audio_size),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = HumanReadable.fileSize(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                state.measurementSize?.let {
                    Row {
                        Text(
                            text = stringResource(Res.string.details_total_size),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = HumanReadable.fileSize(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        NCButton(
                            content = ButtonContent(
                                title = Res.string.details_export_button,
                                icon = Res.drawable.download
                            ),
                            colors = Color.Noise.two.secondaryContainerColors(),
                            onClick = { showExportMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        NCDropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false },
                            colors = Color.Noise.two.secondaryContainerColors(),
                        ) {
                            viewModel.exportMenuItems.forEach { item ->
                                NCDropdownMenuItem(
                                    label = stringResource(item.label),
                                    supportingText = item.supportingText?.let { stringResource(it) },
                                    onClick = {
                                        item.onClick()
                                        showExportMenu = false
                                    },
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        NCButton(
                            content = ButtonContent(
                                title = Res.string.details_delete_button,
                                icon = Res.drawable.delete
                            ),
                            colors = Color.Noise.eight.secondaryContainerColors(),
                            onClick = { showDeleteMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        NCDropdownMenu(
                            expanded = showDeleteMenu,
                            onDismissRequest = { showDeleteMenu = false },
                            colors = Color.Noise.eight.secondaryContainerColors(),
                        ) {
                            viewModel.deleteMenuItems.forEach { item ->
                                NCDropdownMenuItem(
                                    label = stringResource(item.label),
                                    supportingText = item.supportingText?.let { stringResource(it) },
                                    onClick = {
                                        item.onClick()
                                        showDeleteMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        else -> return
    }

    deleteConfirmDialogState?.let {
        NCDialog(
            onDismissRequest = it.onDismissRequest,
            onConfirm = it.onConfirm,
            title = Res.string.details_delete_measurement_dialog_title,
            text = it.text,
            confirmButtonContent = ButtonContent(title = Res.string.delete),
            confirmButtonColors = Color.Noise.eight.secondaryContainerColors(hasDropShadow = true),
            dismissButtonContent = ButtonContent(title = Res.string.cancel),
        )
    }
}
