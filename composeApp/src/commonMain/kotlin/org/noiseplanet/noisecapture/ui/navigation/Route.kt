package org.noiseplanet.noisecapture.ui.navigation

import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import noisecapture.composeapp.generated.resources.measurement_title
import noisecapture.composeapp.generated.resources.platform_info_title
import noisecapture.composeapp.generated.resources.request_permission_title
import noisecapture.composeapp.generated.resources.settings_about_title
import noisecapture.composeapp.generated.resources.settings_help_title
import noisecapture.composeapp.generated.resources.settings_microphone_title
import noisecapture.composeapp.generated.resources.settings_privacy_title
import noisecapture.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.StringResource

enum class Route(val title: StringResource) {
    Home(title = Res.string.app_name),

    PlatformInfo(title = Res.string.platform_info_title),
    RequestPermission(title = Res.string.request_permission_title),
    Measurement(title = Res.string.measurement_title),

    Settings(title = Res.string.settings_title),
    SettingsMicrophone(title = Res.string.settings_microphone_title),
    SettingsPrivacy(title = Res.string.settings_privacy_title),
    SettingsHelp(title = Res.string.settings_help_title),
    SettingsAbout(title = Res.string.settings_about_title),
}
