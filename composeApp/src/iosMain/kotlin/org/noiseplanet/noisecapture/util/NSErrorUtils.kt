package org.noiseplanet.noisecapture.util

import platform.Foundation.NSError

/**
 * Checks an optional [NSError] and if it's not null, throws an [IllegalStateException] with
 * a given message and the error's localized description
 *
 * @param error Optional [NSError]
 * @param lazyMessage Provided error message
 * @throws [IllegalStateException] If given [NSError] is not null.
 */
internal fun checkNoError(error: NSError?, lazyMessage: () -> String) {
    check(error == null) {
        "${lazyMessage()}: ${error?.localizedDescription}"
    }
}
