package org.noiseplanet.noisecapture.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.theme.ColorSet
import org.noiseplanet.noisecapture.ui.theme.Neutral
import org.noiseplanet.noisecapture.ui.theme.Surface
import org.noiseplanet.noisecapture.util.ncDropShadow


object ContainerDefaults {

    val ContentPadding: PaddingValues = PaddingValues(16.dp)
    val Shape: Shape = RoundedCornerShape(16.dp)

    @Composable
    fun colors(): ContainerColors {
        return Color.Neutral.secondaryContainerColors()
    }
}


@Composable
fun Container(
    contentPadding: PaddingValues = ContainerDefaults.ContentPadding,
    shape: Shape = ContainerDefaults.Shape,
    colors: ContainerColors = ContainerDefaults.colors(),
    contentAlignment: Alignment = Alignment.TopStart,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = contentAlignment,
        modifier = modifier
            .semantics { role = Role.Button }
            .ncDropShadow(
                shape = shape,
                color = colors.shadowColor ?: Color.Transparent
            )
            .background(color = colors.backgroundColor, shape = shape)
            .border(
                color = colors.borderColor ?: Color.Transparent,
                width = 1.dp,
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick ?: {})
            .padding(contentPadding)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colors.contentColor,
        ) {
            content()
        }
    }
}


data class ContainerColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val shadowColor: Color? = null,
    val borderColor: Color? = null,
)

fun ColorSet.primaryContainerColors(hasDropShadow: Boolean = true): ContainerColors {
    return ContainerColors(
        backgroundColor = dark,
        contentColor = light,
        shadowColor = if (hasDropShadow) dark else null,
    )
}

fun ColorSet.secondaryContainerColors(hasDropShadow: Boolean = false): ContainerColors {
    return ContainerColors(
        backgroundColor = light,
        contentColor = dark,
        borderColor = mediumLight,
        shadowColor = if (hasDropShadow) dark else null,
    )
}

fun ColorSet.tertiaryContainerColors(hasDropShadow: Boolean = false): ContainerColors {
    return ContainerColors(
        backgroundColor = Color.Surface,
        contentColor = dark,
        borderColor = mediumLight,
        shadowColor = if (hasDropShadow) dark else null,
    )
}

fun ColorSet.transparentContainerColors(): ContainerColors {
    return ContainerColors(
        backgroundColor = Color.Transparent,
        contentColor = dark,
    )
}
