import android.os.Build
import org.noiseplanet.noisecapture.BuildKonfig
import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission

class AndroidPlatform : Platform {

    override val userAgent: UserAgent
        get() = UserAgent(
            versionName = BuildKonfig.versionName,
            versionCode = BuildKonfig.versionCode,
            deviceManufacturer = Build.MANUFACTURER,
            deviceModelName = Build.DEVICE,
            deviceModelCode = Build.PRODUCT,
            osName = "Android",
            osVersion = "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})",
        )

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
