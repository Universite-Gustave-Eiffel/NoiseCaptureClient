package org.noiseplanet.noisecapture.ui.components.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.ContainerColors
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.primaryContainerColors
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.OnSurface
import org.noiseplanet.noisecapture.util.ncDropShadow


@Composable
fun NCDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    containerColors: ContainerColors = NCDialogDefaults.ContainerColors,
    shape: Shape = NCDialogDefaults.Shape,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColors.backgroundColor,
        modifier = modifier
            .border(
                width = 1.dp,
                color = containerColors.borderColor ?: Color.Transparent,
                shape = shape
            )
            .ncDropShadow(
                color = containerColors.shadowColor ?: Color.OnSurface,
                shape = shape,
            ),
    )
}


@Composable
fun NCDialog(
    viewModel: NCDialogViewModel,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit = onDismissRequest,
    modifier: Modifier = Modifier,
) {
    NCDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            viewModel.icon?.let {
                Icon(painterResource(it), contentDescription = null)
            }
        },
        title = {
            Text(
                stringResource(viewModel.title),
                textAlign = TextAlign.Start,
                modifier = modifier.fillMaxWidth()
            )
        },
        text = {
            Text(stringResource(viewModel.text))
        },
        confirmButton = {
            viewModel.confirmButtonContent?.let {
                NCButton(
                    content = viewModel.confirmButtonContent,
                    colors = viewModel.confirmButtonColors,
                    onClick = onConfirm
                )
            }
        },
        dismissButton = {
            viewModel.dismissButtonContent?.let {
                NCButton(
                    content = viewModel.dismissButtonContent,
                    colors = viewModel.dismissButtonColors,
                    onClick = onDismissRequest,
                )
            }
        },
        shape = viewModel.shape,
        containerColors = viewModel.containerColors,
        modifier = modifier,
    )
}


object NCDialogDefaults {

    val Shape = RoundedCornerShape(20.dp)
    val ContainerColors = Color.Noise.two.secondaryContainerColors(hasDropShadow = true)
    val ConfirmButtonColors = Color.Noise.two.primaryContainerColors()
    val DismissButtonColors = Color.Noise.two.transparentContainerColors()
}
