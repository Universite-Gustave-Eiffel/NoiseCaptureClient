package org.noiseplanet.noisecapture.audio.mic

import kotlinx.coroutines.flow.StateFlow


/**
 * Cross platform interface that abstracts getting available input sources and letting the user
 * manually select their preferred microphone.
 */
interface MicrophoneProvider {

    // - Properties

    /**
     * Lists all the currently available input sources.
     */
    val availableDevices: StateFlow<List<MicrophoneInfo>>

    /**
     * Device currently used to stream incoming audio. It is either determined by
     * the system by default, or manually selected by the user.
     */
    val activeDevice: StateFlow<MicrophoneInfo?>


    // - Public functions

    /**
     * Manually select an input source.
     *
     * @param targetDevice Target microphone to be used as input source.
     */
    fun selectDevice(targetDevice: MicrophoneInfo)
}
