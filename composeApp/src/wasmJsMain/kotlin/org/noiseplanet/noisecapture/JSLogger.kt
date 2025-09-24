package org.noiseplanet.noisecapture

import org.noiseplanet.noisecapture.interop.console
import org.noiseplanet.noisecapture.log.LogLevel
import org.noiseplanet.noisecapture.log.Logger


@OptIn(ExperimentalWasmJsInterop::class)
class JSLogger(
    tag: String? = null,
) : Logger(tag) {

    override fun display(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> console.debug(message.toJsString())
            LogLevel.INFO -> console.info(message.toJsString())
            LogLevel.WARNING -> console.warn(message.toJsString())
            LogLevel.ERROR -> console.error(message.toJsString())
        }
    }
}
