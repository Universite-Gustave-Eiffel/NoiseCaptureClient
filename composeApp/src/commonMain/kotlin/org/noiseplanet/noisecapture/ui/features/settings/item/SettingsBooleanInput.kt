package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * Switch that controls a given boolean setting's value.
 */
@Composable
fun SettingsBooleanItem(
    viewModel: SettingsItemViewModel<Boolean>,
    defaultValue: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val value by viewModel.getValueFlow(defaultValue)
        .collectAsState(viewModel.getValue(defaultValue))

    Switch(
        checked = value,
        onCheckedChange = { newValue ->
            viewModel.setValue(newValue)
        },
        modifier
    )
}
