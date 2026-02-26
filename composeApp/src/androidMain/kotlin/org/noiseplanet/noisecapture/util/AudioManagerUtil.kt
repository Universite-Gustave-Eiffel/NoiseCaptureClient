package org.noiseplanet.noisecapture.util

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.model.dao.MicrophoneType


private typealias AndroidMicrophoneInfo = android.media.MicrophoneInfo

/**
 * Gets the input device corresponding to this
 */
fun AudioManager.getInputDevice(deviceId: Int): AudioDeviceInfo? {
    return getDevices(AudioManager.GET_DEVICES_INPUTS).firstOrNull {
        it.id == deviceId
    }
}


/**
 * Maps [android.media.MicrophoneInfo] to [MicrophoneInfo]
 */
@RequiresApi(Build.VERSION_CODES.P)
fun AndroidMicrophoneInfo.toMicrophoneInfo(): MicrophoneInfo {
    return MicrophoneInfo(
        id = id.toString(),
        label = description,
        type = type.toMicrophoneType(),
    )
}

/**
 * Maps [AudioDeviceInfo] to [MicrophoneInfo]
 */
fun AudioDeviceInfo.toMicrophoneInfo(): MicrophoneInfo {
    return MicrophoneInfo(
        id = id.toString(),
        label = productName.toString(),
        type = type.toMicrophoneType(),
    )
}

/**
 * Maps raw device type integer to common [MicrophoneType] enum.
 */
fun Int.toMicrophoneType(): MicrophoneType {
    return when (this) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC,
            -> MicrophoneType.BUILTIN

        AudioDeviceInfo.TYPE_WIRED_HEADSET,
            -> MicrophoneType.WIRED_AUX

        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_ACCESSORY,
            -> MicrophoneType.WIRED_USB

        AudioDeviceInfo.TYPE_BLE_HEADSET,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            -> MicrophoneType.BLUETOOTH

        else -> MicrophoneType.UNKNOWN
    }
}
