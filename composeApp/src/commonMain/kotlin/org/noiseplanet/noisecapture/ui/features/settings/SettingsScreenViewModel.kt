package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Info
import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.settings_about_description
import noisecapture.composeapp.generated.resources.settings_about_title
import noisecapture.composeapp.generated.resources.settings_help_description
import noisecapture.composeapp.generated.resources.settings_help_title
import noisecapture.composeapp.generated.resources.settings_microphone_description
import noisecapture.composeapp.generated.resources.settings_microphone_title
import noisecapture.composeapp.generated.resources.settings_privacy_description
import noisecapture.composeapp.generated.resources.settings_privacy_title
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItemViewModel
import org.noiseplanet.noisecapture.ui.navigation.Route

class SettingsScreenViewModel : ViewModel() {

    val settingsItems: List<SettingsItemViewModel> = listOf(
        SettingsItemViewModel(
            title = Res.string.settings_microphone_title,
            description = Res.string.settings_microphone_description,
            icon = Icons.Filled.Mic,
            target = Route.Settings
        ),
        SettingsItemViewModel(
            title = Res.string.settings_privacy_title,
            description = Res.string.settings_privacy_description,
            icon = Icons.Filled.Security,
            target = Route.Settings
        ),
        SettingsItemViewModel(
            title = Res.string.settings_help_title,
            description = Res.string.settings_help_description,
            icon = Icons.AutoMirrored.Rounded.HelpOutline,
            target = Route.Settings
        ),
        SettingsItemViewModel(
            title = Res.string.settings_about_title,
            description = Res.string.settings_about_description,
            icon = Icons.Outlined.Info,
            target = Route.Settings
        )
    )
}
