package org.noiseplanet.noisecapture.ui.features.details.manage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.arrow_right
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun ManageMeasurementMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    containerColor: Color,
    items: List<ManageMeasurementMenuItem>,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
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
                        painter = painterResource(Res.drawable.arrow_right),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
