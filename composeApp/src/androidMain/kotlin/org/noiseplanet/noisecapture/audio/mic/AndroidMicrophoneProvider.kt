package org.noiseplanet.noisecapture.audio.mic

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.util.toMicrophoneInfo


class AndroidMicrophoneProvider : MicrophoneProvider(), KoinComponent {

    // - Properties

    private val context: Context by inject()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val scope = CoroutineScope(Dispatchers.IO)


    // - Lifecycle

    init {
        audioManager.registerAudioDeviceCallback(
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo?>?) {
                    logger.debug("Added audio devices: $addedDevices")
                    scope.launch { refresh() }
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo?>?) {
                    logger.debug("Removed audio devices: $removedDevices")
                    scope.launch { refresh() }
                }
            },
            null
        )
        scope.launch { refresh() }
    }


    // - Protected functions

    /**
     * Gets a list of all the currently available input sources.
     */
    override suspend fun getCurrentlyAvailableInputs(): List<MicrophoneInfo> {
        val devices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // For recent android versions, use the new MicrophoneInfo API
            audioManager.microphones.sortedBy { it.id }
                .filter {
                    // In devices with API > 31, a new REMOTE_SUBMIX type is introduced, but we don't
                    // want it to be available as input source
                    // https://developer.android.com/reference/android/media/AudioDeviceInfo#TYPE_REMOTE_SUBMIX
                    !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && it.type == AudioDeviceInfo.TYPE_REMOTE_SUBMIX)
                }
                .map { it.toMicrophoneInfo() }
        } else {
            // For older android versions, use AudioDeviceInfo
            audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
                .sortedBy { it.id }
                .map { it.toMicrophoneInfo() }
        }

        // Keep only one input source per type (one builtin, one aux, one usb and one bluetooth)
        return devices.distinctBy { it.type }
    }

    override suspend fun getDefaultInput(): MicrophoneInfo? {
        // Android assigns incremental IDs to external microphones, based on the order they
        // were plugged into the smartphone. So by default, we pick the last added microphone
        // as input source
        return availableInputs.value.lastOrNull()
    }
}
