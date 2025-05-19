package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource


/**
 * Styling and contents of a button
 */
data class ButtonViewModel(
    val title: StringResource?,
    val icon: ImageVector? = null,
    val style: ButtonStyle = ButtonStyle.PRIMARY,
    val hasDropShadow: Boolean = false,
)
