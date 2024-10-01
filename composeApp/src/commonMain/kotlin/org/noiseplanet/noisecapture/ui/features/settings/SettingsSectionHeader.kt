package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsSectionHeader(
    title: StringResource,
) {
    Text(
        text = stringResource(title).uppercase(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp)
    )
}
