package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.util.conditional
import org.noiseplanet.noisecapture.util.shadow.dropShadow

/**
 * A button component to be used throughout the app for unified styling
 */
@Composable
fun NCButton(
    viewModel: ButtonViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val shape: Shape = ButtonDefaults.shape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val icon by viewModel.icon.collectAsState(null)
    val titleResource by viewModel.title.collectAsState(null)
    val title: String? = titleResource?.let { stringResource(it) }

    val finalModifier = modifier.conditional(
        predicate = viewModel.hasDropShadow,
        ifTrue = { dropShadow(shape = shape, isPressed = isPressed) }
    )


    // - Layout

    if (icon != null && title == null) {
        // If only icon is provided, use IconButton as a base
        IconButton(
            onClick = onClick,
            colors = when (viewModel.style) {
                ButtonStyle.PRIMARY -> IconButtonDefaults.filledIconButtonColors()
                ButtonStyle.SECONDARY -> IconButtonDefaults.filledTonalIconButtonColors()
                ButtonStyle.OUTLINED -> IconButtonDefaults.outlinedIconButtonColors()
                ButtonStyle.TEXT -> IconButtonDefaults.iconButtonColors()
            },
            modifier = finalModifier,
        ) {
            NCButtonContents(icon, null)
        }
    } else {
        // Otherwise use a regular button
        Button(
            onClick = onClick,
            colors = when (viewModel.style) {
                ButtonStyle.PRIMARY -> ButtonDefaults.buttonColors()
                ButtonStyle.SECONDARY -> ButtonDefaults.filledTonalButtonColors()
                ButtonStyle.OUTLINED -> ButtonDefaults.outlinedButtonColors()
                ButtonStyle.TEXT -> ButtonDefaults.textButtonColors()
            },
            modifier = finalModifier
        ) {
            NCButtonContents(icon, title)
        }
    }
}


@Composable
private fun NCButtonContents(
    icon: ImageVector?,
    title: String?,
) {
    icon?.let {
        Icon(
            imageVector = it,
            contentDescription = title,
            modifier = Modifier.size(18.dp)
        )
    }

    // If we have both icon and title, add a spacer between the two
    if (icon != null && title != null) {
        Spacer(modifier = Modifier.width(8.dp))
    }

    title?.let { Text(it) }
}
