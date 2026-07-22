package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_body
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_confirm
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_continue
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_title
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCDialog
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise


@Composable
fun EndRecordingConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    // - Layout

    NCDialog(
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        containerColors = Color.Noise.two.secondaryContainerColors(),
        confirmButtonContent = ButtonContent(title = Res.string.measurement_end_confirmation_dialog_confirm),
        confirmButtonColors = Color.Noise.two.tertiaryContainerColors(hasDropShadow = true),
        dismissButtonContent = ButtonContent(title = Res.string.measurement_end_confirmation_dialog_continue),
        dismissButtonColors = Color.Noise.two.transparentContainerColors(),
        title = Res.string.measurement_end_confirmation_dialog_title,
        text = Res.string.measurement_end_confirmation_dialog_body,
    )
}
