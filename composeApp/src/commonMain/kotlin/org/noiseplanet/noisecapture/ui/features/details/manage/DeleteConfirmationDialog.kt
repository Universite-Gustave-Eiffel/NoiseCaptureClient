package org.noiseplanet.noisecapture.ui.features.details.manage

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise


@Composable
fun DeleteConfirmationDialog(
    viewModel: DeleteConfirmationDialogViewModel,
) {
    // - Layout

    AlertDialog(
        onDismissRequest = viewModel.onDismissRequest,
        confirmButton = {
            NCButton(
                onClick = viewModel.onConfirm,
                content = ButtonContent(title = Res.string.delete),
                colors = Color.Noise.eight.transparentContainerColors(),
            )
        },
        dismissButton = {
            NCButton(
                onClick = viewModel.onDismissRequest,
                content = ButtonContent(title = Res.string.cancel),
                colors = Color.Noise.one.transparentContainerColors(),
            )
        },
        title = {
            Text(stringResource(viewModel.title))
        },
        text = {
            Text(stringResource(viewModel.text))
        },
    )
}
