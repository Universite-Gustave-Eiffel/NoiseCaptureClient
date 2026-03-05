package org.noiseplanet.noisecapture.permission.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * Safe wrapper for [UIApplication.openURL] using url string.
 *
 * @param urlString URL string
 * @param completion Completion handler
 */
fun UIApplication.openURL(
    urlString: String,
    completion: (success: Boolean, error: String?) -> Unit,
) {
    val url = NSURL.URLWithString(urlString)
        ?: return completion(false, "Invalid URL: $urlString")

    if (!UIApplication.sharedApplication.canOpenURL(url)) {
        return completion(false, "Cannot open URL: $urlString")
    }
    UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any>()
    ) { success ->
        completion(success, if (success) null else "Could not open URL: $urlString")
    }
}
