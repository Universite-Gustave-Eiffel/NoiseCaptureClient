import android.os.Build
import org.noiseplanet.noisecapture.BuildKonfig
import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.RouteId


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

    override val requiredPermissions: Map<RouteId, List<Permission>>
        get() = super.requiredPermissions + mapOf(
            RouteId.MEASUREMENT_RECORDING to listOf(
                Permission.RECORD_AUDIO,
                Permission.POST_NOTIFICATIONS,
            )
        )
}
