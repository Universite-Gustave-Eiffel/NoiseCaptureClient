package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


data class DeleteConfirmationDialogViewModel(
    val title: StringResource,
    val text: StringResource,
    val onDismissRequest: () -> Unit,
    val onConfirm: () -> Unit,
)


@Composable
fun DeleteConfirmationDialog(
    viewModel: DeleteConfirmationDialogViewModel,
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
        onDismissRequest = viewModel.onDismissRequest,
        confirmButton = {
            NCButton(onClick = viewModel.onConfirm, viewModel = confirmButtonViewModel)
        },
        dismissButton = {
            NCButton(onClick = viewModel.onDismissRequest, viewModel = cancelButtonViewModel)
        },
        title = {
            Text(stringResource(viewModel.title))
        },
        text = {
            Text(stringResource(viewModel.text))
        },
    )
}
