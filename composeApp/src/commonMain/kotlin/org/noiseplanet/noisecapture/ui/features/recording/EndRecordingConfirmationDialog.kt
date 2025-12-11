package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_body
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_confirm
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_continue
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


@Composable
fun EndRecordingConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    // - Properties

    val endRecordingButtonViewModel = NCButtonViewModel(
        title = Res.string.measurement_end_confirmation_dialog_confirm,
        colors = {
            NCButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        },
    )
    val cancelButtonViewModel = NCButtonViewModel(
        title = Res.string.measurement_end_confirmation_dialog_continue,
        style = NCButtonStyle.TEXT,
        colors = { NCButtonColors.Defaults.text() },
    )


    // - Layout

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            NCButton(onClick = onConfirm, viewModel = endRecordingButtonViewModel)
        },
        dismissButton = {
            NCButton(onClick = onDismissRequest, viewModel = cancelButtonViewModel)
        },
        title = {
            Text(stringResource(Res.string.measurement_end_confirmation_dialog_title))
        },
        text = {
            Text(stringResource(Res.string.measurement_end_confirmation_dialog_body))
        },
    )
}
