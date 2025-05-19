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
    val icon by viewModel.icon.collectAsState()
    val title by viewModel.title.collectAsState()
    val style by viewModel.style.collectAsState()

    NCButton(
        onClick = viewModel.onClick,
        viewModel = NCButtonViewModel(
            title = title,
            icon = icon,
            style = style,
            hasDropShadow = viewModel.hasDropShadow
        ),
        modifier = modifier,
    )
}


@Composable
fun NCButton(
    viewModel: NCButtonViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val shape: Shape = ButtonDefaults.shape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val title: String? = viewModel.title?.let { stringResource(it) }

    val finalModifier = modifier.conditional(
        predicate = viewModel.hasDropShadow,
        ifTrue = { dropShadow(shape = shape, isPressed = isPressed) }
    )


    // - Layout

    AnimatedContent(
        targetState = viewModel,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { viewModel ->
        if (viewModel.icon != null && title == null) {
            // If only icon is provided, use IconButton as a base
            when (viewModel.style) {
                ButtonStyle.PRIMARY -> FilledIconButton(
                    onClick,
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, null) }

                ButtonStyle.SECONDARY -> FilledTonalIconButton(
                    onClick,
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, null) }

                ButtonStyle.OUTLINED -> OutlinedIconButton(
                    onClick,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, null) }

                ButtonStyle.TEXT -> IconButton(
                    onClick,
                    colors = IconButtonDefaults.outlinedIconButtonColors(),
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, null) }
            }
        } else {
            // Otherwise use a regular button
            when (viewModel.style) {
                ButtonStyle.PRIMARY -> Button(
                    onClick,
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, title) }

                ButtonStyle.SECONDARY -> FilledTonalButton(
                    onClick,
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, title) }

                ButtonStyle.OUTLINED -> OutlinedButton(
                    onClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, title) }

                ButtonStyle.TEXT -> TextButton(
                    onClick,
                    modifier = finalModifier
                ) { NCButtonContents(viewModel.icon, title) }
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
