package org.noiseplanet.noisecapture

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import org.noiseplanet.noisecapture.log.LogLevel
import org.noiseplanet.noisecapture.log.Logger
import platform.darwin.OS_LOG_DEFAULT
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_FAULT
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal

/**
 * iOS Koin logger implementation that relies on native OSLog
 */
@OptIn(ExperimentalForeignApi::class)
class IOSLogger(
    tag: String? = null,
) : Logger(tag) {

    override fun display(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> iosDebug(message)
            LogLevel.INFO -> iosInfo(message)
            LogLevel.WARNING -> iosWarn(message)
            LogLevel.ERROR -> iosError(message)
        }
    }

    private fun iosDebug(msg: String) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_INFO,
            msg
        )
    }

    private fun iosInfo(msg: String) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_DEFAULT,
            msg
        )
    }

    private fun iosWarn(msg: String) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_ERROR,
            msg
        )
    }

    private fun iosError(msg: String) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_FAULT,
            msg
        )
    }
}
