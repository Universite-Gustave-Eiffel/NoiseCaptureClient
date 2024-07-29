package org.noiseplanet.noisecapture.interop

/**
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console)
 */
external class Console {

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console/debug_static)
     */
    fun debug(msg: JsString)

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console/info_static)
     */
    fun info(msg: JsString)

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console/log_static)
     */
    fun log(msg: JsString)

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console/warn_static)
     */
    fun warn(msg: JsString)

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console/error_static)
     */
    fun error(msg: JsString)
}

/**
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/console)
 */
external val console: Console
