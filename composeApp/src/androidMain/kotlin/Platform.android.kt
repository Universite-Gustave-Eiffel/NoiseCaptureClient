import android.os.Build
import org.noiseplanet.noisecapture.permission.Permission

class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val requiredPermissions: List<Permission>
        /**
         * We have to request foreground location before asking for background location
         */
        get() = listOf(
            Permission.RECORD_AUDIO,
            Permission.LOCATION_SERVICE_ON,
            Permission.LOCATION_FOREGROUND,
            Permission.LOCATION_BACKGROUND,
            Permission.POST_NOTIFICATIONS,
        )
}
