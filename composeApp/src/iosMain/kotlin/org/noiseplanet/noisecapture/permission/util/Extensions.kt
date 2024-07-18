package org.noiseplanet.noisecapture.permission.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal fun openNSUrl(string: String) {
    val settingsUrl: NSURL = requireNotNull(NSURL.URLWithString(string)) {
        throw CannotOpenSettingsException(string)
    }
    if (UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
        UIApplication.sharedApplication.openURL(settingsUrl)
    } else {
        throw CannotOpenSettingsException(string)
    }
}

internal fun openAppSettingsPage() {
    openNSUrl(UIApplicationOpenSettingsURLString)
}
