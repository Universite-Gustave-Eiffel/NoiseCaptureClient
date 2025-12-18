package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


data class MenuItem(
    val label: StringResource,
    val supportingText: StringResource?,
    val onClick: () -> Unit,
)


@Composable
fun ManageMeasurementMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    containerColor: Color,
    items: List<MenuItem>,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            .fillMaxWidth(fraction = 0.8f)
    ) {
        for (item in items) {
            DropdownMenuItem(
                text = {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(item.label),
                            style = MaterialTheme.typography.labelLarge
                        )
                        item.supportingText?.let { supportingText ->
                            Text(
                                text = stringResource(supportingText),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                onClick = {
                    item.onClick()
                    onDismissRequest()
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
