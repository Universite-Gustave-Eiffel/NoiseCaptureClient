package org.noiseplanet.noisecapture.ui.components.dialog

import androidx.compose.ui.graphics.Shape
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.ContainerColors


data class NCDialogViewModel(
    val title: StringResource,
    val text: StringResource,
    val confirmButtonContent: ButtonContent? = null,
    val dismissButtonContent: ButtonContent? = null,
    val confirmButtonColors: ContainerColors = NCDialogDefaults.ConfirmButtonColors,
    val dismissButtonColors: ContainerColors = NCDialogDefaults.DismissButtonColors,
    val containerColors: ContainerColors = NCDialogDefaults.ContainerColors,
    val shape: Shape = NCDialogDefaults.Shape,
    val icon: DrawableResource? = null,
)
