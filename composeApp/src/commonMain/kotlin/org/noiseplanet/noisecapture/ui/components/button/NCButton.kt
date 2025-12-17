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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    viewModel: NCButtonViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val shape: Shape = ButtonDefaults.shape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val colors = viewModel.colors()

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
                NCButtonStyle.FILLED -> FilledIconButton(
                    onClick,
                    colors = colors.toIconButtonColors(),
                    modifier = finalModifier
                ) {
                    NCButtonContents(
                        viewModel.icon,
                        viewModel.iconPlacement,
                        null,
                        colors.contentColor
                    )
                }

                NCButtonStyle.OUTLINED -> OutlinedIconButton(
                    onClick,
                    colors = colors.toIconButtonColors()
                        .copy(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                    border = BorderStroke(width = 2.dp, color = colors.containerColor),
                    modifier = finalModifier
                ) {
                    NCButtonContents(
                        viewModel.icon,
                        viewModel.iconPlacement,
                        null,
                        colors.contentColor
                    )
                }

                NCButtonStyle.TEXT -> IconButton(
                    onClick,
                    colors = colors.toIconButtonColors()
                        .copy(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                    modifier = finalModifier
                ) {
                    NCButtonContents(
                        viewModel.icon,
                        viewModel.iconPlacement,
                        null,
                        colors.contentColor
                    )
                }
            }
        } else {
            // Otherwise use a regular button
            when (viewModel.style) {
                NCButtonStyle.FILLED -> Button(
                    onClick,
                    colors = colors.toButtonColors(),
                    modifier = finalModifier,
                ) {
                    NCButtonContents(
                        viewModel.icon,
                        viewModel.iconPlacement,
                        title,
                        colors.contentColor
                    )
                }

                NCButtonStyle.OUTLINED -> OutlinedButton(
                    onClick,
                    colors = colors.toButtonColors()
                        .copy(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                    border = BorderStroke(width = 2.dp, color = colors.containerColor),
                    modifier = finalModifier,
                ) {
                    NCButtonContents(
                        viewModel.icon,
                        viewModel.iconPlacement,
                        title,
                        colors.contentColor
                    )
                }

                NCButtonStyle.TEXT -> TextButton(
                    onClick,
                    colors = colors.toButtonColors()
                        .copy(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                    modifier = finalModifier
                ) {
                    NCButtonContents(
                        viewModel.icon,
                        viewModel.iconPlacement,
                        title,
                        colors.contentColor
                    )
                }
            }
        }
    }
}


@Composable
private fun NCButtonContents(
    icon: ImageVector?,
    iconPlacement: NCButtonIconPlacement,
    title: String?,
    contentColor: Color,
) {
    if (icon != null && iconPlacement == NCButtonIconPlacement.START) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )

        if (title != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }

    title?.let {
        Text(it, color = contentColor)
    }

    if (icon != null && iconPlacement == NCButtonIconPlacement.END) {
        if (title != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
    }
}
