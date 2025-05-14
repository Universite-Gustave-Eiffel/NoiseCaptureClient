package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource


/**
 * Styling and contents of a button
 */
data class ButtonViewModel(
    val onClick: () -> Unit,
    val title: StateFlow<StringResource?> = MutableStateFlow(null),
    val icon: StateFlow<ImageVector?> = MutableStateFlow(null),
    val style: StateFlow<ButtonStyle> = MutableStateFlow(ButtonStyle.PRIMARY),
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
        MutableStateFlow(title),
        MutableStateFlow(icon),
        MutableStateFlow(style),
        hasDropShadow
    )
}
