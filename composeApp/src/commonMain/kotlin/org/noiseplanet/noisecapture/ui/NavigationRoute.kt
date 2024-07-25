package org.noiseplanet.noisecapture.ui

import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import noisecapture.composeapp.generated.resources.measurement_title
import noisecapture.composeapp.generated.resources.platform_info_title
import noisecapture.composeapp.generated.resources.request_permission_title
import org.jetbrains.compose.resources.StringResource

enum class NavigationRoute(val title: StringResource) {
    Home(title = Res.string.app_name),
    PlatformInfo(title = Res.string.platform_info_title),
    RequestPermission(title = Res.string.request_permission_title),
    Measurement(title = Res.string.measurement_title)
}
