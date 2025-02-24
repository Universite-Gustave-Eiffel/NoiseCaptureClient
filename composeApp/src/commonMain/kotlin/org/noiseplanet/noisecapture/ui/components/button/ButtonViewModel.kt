package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.StringResource


/**
 * Styling and contents of a button
 */
data class ButtonViewModel(
    val onClick: () -> Unit,
    val title: Flow<StringResource?> = flowOf(null),
    val icon: Flow<ImageVector?> = flowOf(null),
    val style: Flow<ButtonStyle> = flowOf(ButtonStyle.PRIMARY),
    val hasDropShadow: Boolean = false,
) {
    // - Lifecycle

    /**
     * Convenience initializer for buttons where properties are not state dependant.
     */
    constructor(
        onClick: () -> Unit,
        title: StringResource? = null,
        icon: ImageVector? = null,
        style: ButtonStyle = ButtonStyle.PRIMARY,
        hasDropShadow: Boolean = false,
    ) : this(
        onClick,
        flowOf(title),
        flowOf(icon),
        flowOf(style),
        hasDropShadow
    )
}
