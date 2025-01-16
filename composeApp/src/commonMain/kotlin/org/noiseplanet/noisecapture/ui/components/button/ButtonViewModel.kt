package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.StringResource


/**
 * Styling and contents of a button
 */
data class ButtonViewModel(
    val title: Flow<StringResource?> = flow { emit(null) },
    val icon: Flow<ImageVector?> = flow { emit(null) },
    val style: ButtonStyle = ButtonStyle.PRIMARY,
    val hasDropShadow: Boolean = false,
) {

    /**
     * Convenience initializer for buttons where title and icons are not state dependant.
     */
    constructor(
        title: StringResource? = null,
        icon: ImageVector? = null,
        style: ButtonStyle = ButtonStyle.PRIMARY,
        hasDropShadow: Boolean = false,
    ) : this(
        flow { emit(title) },
        flow { emit(icon) },
        style,
        hasDropShadow
    )
}
