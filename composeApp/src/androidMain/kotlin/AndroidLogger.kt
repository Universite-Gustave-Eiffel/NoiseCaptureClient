import android.util.Log
import org.noiseplanet.noisecapture.log.LogLevel
import org.noiseplanet.noisecapture.log.Logger

class AndroidLogger(
    tag: String? = null,
) : Logger(tag) {

    override fun display(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARNING -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }
}
