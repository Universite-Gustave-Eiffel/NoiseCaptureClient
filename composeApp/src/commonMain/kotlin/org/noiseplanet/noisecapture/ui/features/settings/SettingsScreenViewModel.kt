package org.noiseplanet.noisecapture.ui.features.settings

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.settings_about_title
import noisecapture.composeapp.generated.resources.settings_help_title
import noisecapture.composeapp.generated.resources.settings_microphone_description
import noisecapture.composeapp.generated.resources.settings_microphone_title
import noisecapture.composeapp.generated.resources.settings_privacy_description
import noisecapture.composeapp.generated.resources.settings_privacy_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.services.SettingsKey
import org.noiseplanet.noisecapture.services.UserSettingsService
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItemViewModel

class SettingsScreenViewModel(
    private val settingsService: UserSettingsService,
) : ViewModel() {

    val settingsItems: Map<StringResource, List<SettingsItemViewModel<*>>> = mapOf(

        // TODO: Fill in with actual settings values instead of placeholders

        Pair(
            Res.string.settings_about_title, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_microphone_title,
                    description = Res.string.settings_microphone_description,
                    settingKey = SettingsKey.Test,
                    settingsService = settingsService,
                    isFirstInSection = true,
                    isLastInSection = true,
                ),
            )
        ),
        Pair(
            Res.string.settings_help_title, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_privacy_title,
                    description = Res.string.settings_privacy_description,
                    settingKey = SettingsKey.TooltipsEnabled,
                    settingsService = settingsService,
                    isFirstInSection = true,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_microphone_title,
                    description = Res.string.settings_microphone_description,
                    settingKey = SettingsKey.DisclaimersEnabled,
                    settingsService = settingsService,
                    isLastInSection = true,
                ),
            )
        )
    )
}
