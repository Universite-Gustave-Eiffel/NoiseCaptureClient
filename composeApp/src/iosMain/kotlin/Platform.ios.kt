import androidx.compose.ui.graphics.ImageBitmapConfig
import kotlinx.cinterop.ExperimentalForeignApi
import org.noiseplanet.noisecapture.DeviceUtil
import org.noiseplanet.noisecapture.model.dao.UserAgent
import platform.UIKit.UIDevice

class IOSPlatform : Platform {

    // - Platform

    /**
     * Bitmap config Rgb565 is not supported on iOS simulator so we need to fallback on using Argb8888.
     * https://developer.apple.com/documentation/metal/developing-metal-apps-that-run-in-simulator#Texture-limitations
     */
    @OptIn(ExperimentalForeignApi::class)
    override val bitmapConfig: ImageBitmapConfig
        get() = if (DeviceUtil.isRunningOnSimulator()) {
            ImageBitmapConfig.Argb8888
        } else {
            ImageBitmapConfig.Rgb565
        }

    override val userAgent: UserAgent
        get() = UserAgent(
            deviceManufacturer = "Apple",
            deviceModelCode = UIDevice.currentDevice.model,
            deviceModelName = UIDevice.currentDevice.name,
            osName = UIDevice.currentDevice.systemName,
            osVersion = UIDevice.currentDevice.systemVersion,
        )
}
