package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource


enum class NCButtonIconPlacement {
    START,
    END,
}


/**
 * Styling and contents of a button
 */
open class NCButtonViewModel(
    val title: StringResource?,
    val icon: ImageVector? = null,
    val iconPlacement: NCButtonIconPlacement = NCButtonIconPlacement.START,
    val style: NCButtonStyle = NCButtonStyle.FILLED,
    val colors: @Composable () -> NCButtonColors = { NCButtonColors.Defaults.primary() },
    val hasDropShadow: Boolean = false,
)


/**
 * Utility subclass of [NCButtonViewModel] for only displaying an icon with no title.
 */
class IconNCButtonViewModel(
    icon: ImageVector? = null,
    style: NCButtonStyle = NCButtonStyle.FILLED,
    colors: @Composable () -> NCButtonColors = { NCButtonColors.Defaults.primary() },
    hasDropShadow: Boolean = false,
) : NCButtonViewModel(
    title = null,
    icon = icon,
    style = style,
    colors = colors,
    hasDropShadow = hasDropShadow,
)
