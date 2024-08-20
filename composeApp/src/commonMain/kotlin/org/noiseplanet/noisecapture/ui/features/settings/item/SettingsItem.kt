package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.CardView
import org.noiseplanet.noisecapture.ui.navigation.Route

@Composable
fun SettingsItem(
    viewModel: SettingsItemViewModel,
    navigateTo: (Route) -> Unit,
) {
    CardView(onClick = {
        navigateTo(viewModel.target)
    }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(viewModel.icon, contentDescription = stringResource(viewModel.title))

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    stringResource(viewModel.title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(viewModel.description),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Image(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
            )
        }
    }
}
