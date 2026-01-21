package org.noiseplanet.noisecapture.ui.features.details.manage

import org.jetbrains.compose.resources.StringResource

data class DeleteConfirmationDialogViewModel(
    val title: StringResource,
    val text: StringResource,
    val onDismissRequest: () -> Unit,
    val onConfirm: () -> Unit,
)
