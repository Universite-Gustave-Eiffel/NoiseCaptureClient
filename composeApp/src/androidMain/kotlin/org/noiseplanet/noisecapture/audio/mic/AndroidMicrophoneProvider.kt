package org.noiseplanet.noisecapture.audio.mic

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger


private typealias AndroidMicrophoneInfo = android.media.MicrophoneInfo


class AndroidMicrophoneProvider : MicrophoneProvider, KoinComponent {

    // - Properties

    private val context: Context by inject()
    private val logger: Logger by injectLogger()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _availableDevices = MutableStateFlow<List<MicrophoneInfo>>(emptyList())
    override val availableDevices: StateFlow<List<MicrophoneInfo>>
        get() = _availableDevices

    /**
     * Holds the audio device currently selected by the user, or null if system device
     * should be used instead.
     */
    private var userSelectedDevice: MicrophoneInfo? = null

    private val _activeDevice = MutableStateFlow<MicrophoneInfo?>(null)
    override val activeDevice: StateFlow<MicrophoneInfo?>
        get() = _activeDevice


    // - Lifecycle

    init {
        audioManager.registerAudioDeviceCallback(
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo?>?) {
                    logger.debug("Added audio devices: $addedDevices")
                    refresh()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo?>?) {
                    logger.debug("Removed audio devices: $removedDevices")
                    refresh()
                }
            },
            null
        )
        refresh()
    }


    // - Public functions

    override fun selectDevice(targetDevice: MicrophoneInfo) {
        if (targetDevice.id !in _availableDevices.value.map { it.id }) {
            logger.warning("Trying to select unavailable input source: $targetDevice")
            return
        }
        userSelectedDevice = targetDevice
        refresh()
    }


    // - Private functions

    /**
     * Refreshes the currently available input sources, current active device and user selected
     * device if needed.
     */
    private fun refresh() {
        // Refresh the list of available devices
        val currentDevices = getCurrentlyAvailableDevices()
        _availableDevices.tryEmit(currentDevices)

        // If no device is available, set user device and active device to null
        if (currentDevices.isEmpty()) {
            userSelectedDevice = null
            _activeDevice.tryEmit(null)
        }

        // Android assigns incremental IDs to external microphones, based on the order they
        // were plugged into the smartphone. So by default, we pick the last added microphone
        // as input source
        val defaultDevice = currentDevices.last()

        userSelectedDevice?.id?.let { selectedDeviceId ->
            // If user manually selected a device and it is not available anymore, switch to
            // the next default input source
            if (selectedDeviceId !in currentDevices.map { it.id }) {
                userSelectedDevice = null
                _activeDevice.tryEmit(defaultDevice)
            } else if (selectedDeviceId != _activeDevice.value?.id) {
                // If available (and not already in use), use it as active input source
                _activeDevice.tryEmit(userSelectedDevice)
            }
        } ?: run {
            // If no input source was manually selected, use the default one
            _activeDevice.tryEmit(defaultDevice)
        }
        // If manually selected input source is still available, don't change the active device
    }

    /**
     * Gets a list of all the currently available input sources.
     */
    private fun getCurrentlyAvailableDevices(): List<MicrophoneInfo> {
        val devices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // For recent android versions, use the new MicrophoneInfo API
            audioManager.microphones.sortedBy { it.id }
                .map { it.toMicrophoneInfo() }
        } else {
            // For older android versions, use AudioDeviceInfo
            audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
                .sortedBy { it.id }
                .map { it.toMicrophoneInfo() }
        }

        // Filter out input devices that didn't match any supported input types
        // and keep only one input source per type (one builtin, one aux, one usb and one bluetooth)
        return devices.filterNot { it.type == MicrophoneType.UNKNOWN }
            .distinctBy { it.type }
    }
}


/**
 * Maps [android.media.MicrophoneInfo] to [MicrophoneInfo]
 */
@RequiresApi(Build.VERSION_CODES.P)
private fun AndroidMicrophoneInfo.toMicrophoneInfo(): MicrophoneInfo {
    return MicrophoneInfo(
        id = id.toString(),
        name = description,
        type = type.toMicrophoneType(),
    )
}

/**
 * Maps [AudioDeviceInfo] to [MicrophoneInfo]
 */
private fun AudioDeviceInfo.toMicrophoneInfo(): MicrophoneInfo {
    return MicrophoneInfo(
        id = id.toString(),
        name = productName.toString(),
        type = type.toMicrophoneType(),
    )
}

/**
 * Maps raw device type integer to common [MicrophoneType] enum.
 */
private fun Int.toMicrophoneType(): MicrophoneType {
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
