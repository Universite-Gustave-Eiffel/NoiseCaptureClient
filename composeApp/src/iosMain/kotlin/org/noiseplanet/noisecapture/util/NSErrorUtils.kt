package org.noiseplanet.noisecapture.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value
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


/**
 * Utility function to run Obj-C methods that take an error pointer as second argument. Instead
 * of manually creating error pointer, pass it to the method and check for result, this is wrapped
 * into a [runCatching] block that returns result of [block] on success
 *
 * @param block Throwable block to execute
 *
 * @return Result of internal [runCatching]
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun <T, R> T.runCatchingNSError(
    block: T.(errorPtr: ObjCObjectVar<NSError?>) -> R,
): Result<R> {
    // Catch exception in result with runCatching
    return runCatching {
        memScoped {
            // Create NSError pointer
            val error: ObjCObjectVar<NSError?> = alloc()

            // Run block with error pointer
            val result = block(error)

            // Check for error and throw exception if not null
            check(error.value == null) { error.value?.localizedDescription ?: "Unknown error" }

            // Return result on success
            result
        }
    }
}
