package org.noiseplanet.noisecapture.log

/**
 * Logger interface that should be implemented for each platform using native logging capabilities
 *
 * @param tag Tag that will be prefixed before each message logged by this instance.
 *            Should give context information about the caller (e.g. class name).
 */
abstract class Logger(
    // TODO: Is there a way to infer class name at build time?
    private val tag: String? = null,
) {

    /**
     * Calls native logger implementation to display the given message from the given log level.
     *
     * Note: Log level is already written in the message, this property should just be used to
     *       determine which internal logger method should be called
     *
     *  @param level: Log level
     *  @param message: Message to be displayed
     */
    protected abstract fun display(level: LogLevel, message: String)

    /**
     * Logs a debug message.
     *
     * @param message Message to be displayed.
     */
    fun debug(message: String) {
        val level = LogLevel.DEBUG
        display(
            level,
            message(level.shortName, message)
        )
    }

    /**
     * Logs an information message.
     *
     * @param message Message to be displayed.
     */
    fun info(message: String) {
        val level = LogLevel.INFO
        display(
            level,
            message(level.shortName, message)
        )
    }

    /**
     * Logs a warning message.
     *
     * @param message Message to be displayed.
     * @param throwable Optional throwable, if given the stack trace will be appended to the message.
     */
    fun warning(message: String, throwable: Throwable? = null) {
        val level = LogLevel.WARNING
        display(
            level,
            message(level.shortName, message, throwable)
        )
    }

    /**
     * Logs an error message.
     *
     * @param message Message to be displayed.
     * @param throwable Optional throwable, if given the stack trace will be appended to the message.
     */
    fun error(message: String, throwable: Throwable? = null) {
        val level = LogLevel.ERROR
        display(
            level,
            message(level.shortName, message, throwable)
        )
    }

    /**
     * Formats the given message before logging.
     */
    private fun message(
        level: String,
        msg: String,
        t: Throwable? = null,
    ): String {
        val str = if (tag != null) {
            "[$tag] $level: $msg"
        } else {
            "$level: $msg"
        }
        return if (t == null) str else "$str $t"
    }
}
