package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource


/**
 * Styling and contents of a button
 */
open class ButtonViewModel(
    val title: StringResource?,
    val icon: ImageVector? = null,
    val style: ButtonStyle = ButtonStyle.PRIMARY,
    val hasDropShadow: Boolean = false,
)


/**
 * Utility subclass of [ButtonViewModel] for only displaying an icon with no title.
 */
class IconButtonViewModel(
    icon: ImageVector? = null,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    hasDropShadow: Boolean = false,
) : ButtonViewModel(
    title = null,
    icon = icon,
    style = style,
    hasDropShadow = hasDropShadow
)
