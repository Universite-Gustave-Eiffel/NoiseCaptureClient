package org.noiseplanet.noisecapture.ui.components.appbar

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class AppBarButtonViewModel(
    val icon: DrawableResource,
    val iconContentDescription: StringResource? = null,
    val onClick: () -> Unit,
)
