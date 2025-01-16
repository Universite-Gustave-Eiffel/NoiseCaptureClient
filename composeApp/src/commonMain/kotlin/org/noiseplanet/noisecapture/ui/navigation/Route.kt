package org.noiseplanet.noisecapture.ui.navigation

import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import noisecapture.composeapp.generated.resources.measurement_title
import noisecapture.composeapp.generated.resources.platform_info_title
import noisecapture.composeapp.generated.resources.request_permission_title
import noisecapture.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.StringResource

/**
 * Describes the different navigation routes (i.e. screens)
 *
 * @param title Title that will be displayed in the navigation bar
 * @param usesAudioSource If set to true, audio source will be started automatically when
 *                        navigating to this screen. If false, it will be paused instead.
 */
enum class Route(
    val title: StringResource,
    val usesAudioSource: Boolean = false,
) {

    Home(title = Res.string.app_name, usesAudioSource = true),

    PlatformInfo(title = Res.string.platform_info_title),
    RequestPermission(title = Res.string.request_permission_title),
    Measurement(title = Res.string.measurement_title, usesAudioSource = true),

    Settings(title = Res.string.settings_title),
}
