package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_body
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_confirm
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_continue
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise


@Composable
fun EndRecordingConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    // - Layout

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            NCButton(
                onClick = onConfirm,
                content = ButtonContent(title = Res.string.measurement_end_confirmation_dialog_confirm),
                colors = Color.Noise.eight.secondaryContainerColors()
            )
        },
        dismissButton = {
            NCButton(
                onClick = onDismissRequest,
                content = ButtonContent(title = Res.string.measurement_end_confirmation_dialog_continue),
                colors = Color.Noise.one.transparentContainerColors()
            )
        },
        title = {
            Text(stringResource(Res.string.measurement_end_confirmation_dialog_title))
        },
        text = {
            Text(stringResource(Res.string.measurement_end_confirmation_dialog_body))
        },
    )
}
