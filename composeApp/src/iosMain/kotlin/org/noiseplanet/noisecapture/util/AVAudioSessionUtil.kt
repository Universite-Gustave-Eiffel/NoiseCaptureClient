package org.noiseplanet.noisecapture.util

import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.model.dao.MicrophoneType
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPort
import platform.AVFAudio.AVAudioSessionPortBluetoothA2DP
import platform.AVFAudio.AVAudioSessionPortBluetoothHFP
import platform.AVFAudio.AVAudioSessionPortBluetoothLE
import platform.AVFAudio.AVAudioSessionPortBuiltInMic
import platform.AVFAudio.AVAudioSessionPortContinuityMicrophone
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.AVAudioSessionPortFireWire
import platform.AVFAudio.AVAudioSessionPortHeadsetMic
import platform.AVFAudio.AVAudioSessionPortLineIn
import platform.AVFAudio.AVAudioSessionPortThunderbolt
import platform.AVFAudio.AVAudioSessionPortUSBAudio
import platform.AVFAudio.availableInputs


/**
 * Returns the [AVAudioSessionPortDescription] with the corresponding id, or null if it
 * wasn't found among the currently available input sources.
 *
 * @param id [AVAudioSessionPortDescription] UID.
 * @return [AVAudioSessionPortDescription] matching the given UID, null if not found.
 */
fun AVAudioSession.getInput(id: String): AVAudioSessionPortDescription? {
    return availableInputs?.map {
        it as? AVAudioSessionPortDescription
    }?.firstOrNull {
        it?.UID == id
    }
}

/**
 * Maps iOS's [AVAudioSessionPortDescription] to shared [MicrophoneInfo] interface.
 */
fun AVAudioSessionPortDescription.toMicrophoneInfo(): MicrophoneInfo {
    return MicrophoneInfo(
        id = UID,
        label = portName,
        type = portType.toMicrophoneType()
    )
}

/**
 * Maps iOS's [AVAudioSessionPort] to common [MicrophoneType] enum.
 */
fun AVAudioSessionPort.toMicrophoneType(): MicrophoneType {
    return when (this) {
        AVAudioSessionPortBuiltInMic,
            -> MicrophoneType.BUILTIN

        AVAudioSessionPortHeadsetMic,
        AVAudioSessionPortLineIn,
            -> MicrophoneType.WIRED_AUX

        AVAudioSessionPortBluetoothLE,
        AVAudioSessionPortBluetoothA2DP,
        AVAudioSessionPortBluetoothHFP,
        AVAudioSessionPortContinuityMicrophone,
            -> MicrophoneType.BLUETOOTH

        AVAudioSessionPortUSBAudio,
        AVAudioSessionPortFireWire,
        AVAudioSessionPortThunderbolt,
            -> MicrophoneType.WIRED_USB

        else -> MicrophoneType.UNKNOWN
    }
}
