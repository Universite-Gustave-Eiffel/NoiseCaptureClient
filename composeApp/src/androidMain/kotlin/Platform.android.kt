import android.os.Build
import org.noiseplanet.noisecapture.BuildKonfig
import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.MeasurementRecordingRoute

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

    override val requiredPermissions: Map<String, List<Permission>>
        get() = super.requiredPermissions + mapOf(
            MeasurementRecordingRoute().id to listOf(
                Permission.RECORD_AUDIO,
                Permission.POST_NOTIFICATIONS,
            )
        )
}
