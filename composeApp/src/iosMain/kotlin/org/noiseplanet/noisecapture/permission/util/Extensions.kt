package org.noiseplanet.noisecapture.permission.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal fun openNSUrl(string: String) {
    val settingsUrl: NSURL = NSURL.URLWithString(string)!!
    if (UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
        UIApplication.sharedApplication.openURL(settingsUrl)
    } else {
        throw CannotOpenSettingsException(string)
    }
}

internal fun openAppSettingsPage() {
    openNSUrl(UIApplicationOpenSettingsURLString)
}

// internal fun CoroutineScope.observePermission(
//    frequency: Long = PermissionsService.PERMISSION_CHECK_FLOW_FREQUENCY,
//    block: suspend () -> Unit,
// ): Job = launch {
//    while (true) {
//        block()
//        delay(frequency)
//    }
// }
