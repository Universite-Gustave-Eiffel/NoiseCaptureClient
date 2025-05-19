package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    modifier: Modifier = Modifier,
) {
    // - Properties

    val shape: Shape = ButtonDefaults.shape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val icon by viewModel.icon.collectAsState()
    val titleResource by viewModel.title.collectAsState()
    val title: String? = titleResource?.let { stringResource(it) }
    val style by viewModel.style.collectAsState()

    val finalModifier = modifier.conditional(
        predicate = viewModel.hasDropShadow,
        ifTrue = { dropShadow(shape = shape, isPressed = isPressed) }
    )


    // - Layout

    AnimatedContent(
        targetState = style,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { targetStyle ->
        if (icon != null && title == null) {
            // If only icon is provided, use IconButton as a base
            when (targetStyle) {
                ButtonStyle.PRIMARY -> FilledIconButton(
                    viewModel.onClick,
                    modifier = finalModifier
                ) { NCButtonContents(icon, null) }

                ButtonStyle.SECONDARY -> FilledTonalIconButton(
                    viewModel.onClick,
                    modifier = finalModifier
                ) { NCButtonContents(icon, null) }

                ButtonStyle.OUTLINED -> OutlinedIconButton(
                    viewModel.onClick,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
                    modifier = finalModifier
                ) { NCButtonContents(icon, null) }

                ButtonStyle.TEXT -> IconButton(
                    viewModel.onClick,
                    colors = IconButtonDefaults.outlinedIconButtonColors(),
                    modifier = finalModifier
                ) { NCButtonContents(icon, null) }
            }
        } else {
            // Otherwise use a regular button
            when (targetStyle) {
                ButtonStyle.PRIMARY -> Button(
                    viewModel.onClick,
                    modifier = finalModifier
                ) { NCButtonContents(icon, title) }

                ButtonStyle.SECONDARY -> FilledTonalButton(
                    viewModel.onClick,
                    modifier = finalModifier
                ) { NCButtonContents(icon, title) }

                ButtonStyle.OUTLINED -> OutlinedButton(
                    viewModel.onClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
                    modifier = finalModifier
                ) { NCButtonContents(icon, title) }

                ButtonStyle.TEXT -> TextButton(
                    viewModel.onClick,
                    modifier = finalModifier
                ) { NCButtonContents(icon, title) }
            }
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
