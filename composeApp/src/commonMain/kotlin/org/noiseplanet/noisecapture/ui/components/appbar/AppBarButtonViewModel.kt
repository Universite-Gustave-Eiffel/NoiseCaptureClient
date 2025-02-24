package org.noiseplanet.noisecapture.ui.components.appbar

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

data class AppBarButtonViewModel(
    val icon: ImageVector,
    val iconContentDescription: StringResource? = null,
    val onClick: () -> Unit,
)
