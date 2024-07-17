package org.noiseplanet.noisecapture.ui

import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import noisecapture.composeapp.generated.resources.platform_info_title
import org.jetbrains.compose.resources.StringResource

enum class NavigationRoute(val title: StringResource) {
    Home(title = Res.string.app_name),
    PlatformInfo(title = Res.string.platform_info_title)
}