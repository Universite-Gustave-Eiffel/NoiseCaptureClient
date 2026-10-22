package org.noiseplanet.noisecapture.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.arrow_right
import org.jetbrains.compose.resources.painterResource
import org.noiseplanet.noisecapture.ui.theme.Noise


@Composable
fun NCDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    shape: Shape = NCDropdownMenuDefaults.Shape,
    colors: ContainerColors = NCDropdownMenuDefaults.Colors,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = colors.backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = shape,
        content = content,
        modifier = modifier
            .background(color = colors.backgroundColor, shape = shape)
            .border(
                width = 1.dp,
                color = colors.borderColor ?: Color.Transparent,
                shape = MaterialTheme.shapes.large
            ),
    )
}


@Composable
fun NCDropdownMenuItem(
    label: String,
    supportingText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
                supportingText?.let {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        onClick = {
            onClick()
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.arrow_right),
                contentDescription = null,
            )
        },
        modifier = modifier,
    )
}

object NCDropdownMenuDefaults {

    val Colors: ContainerColors = Color.Noise.two.secondaryContainerColors()
    val Shape: Shape = ContainerDefaults.Shape
}
