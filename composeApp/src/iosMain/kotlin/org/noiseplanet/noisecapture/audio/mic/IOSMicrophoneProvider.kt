package org.noiseplanet.noisecapture.audio.mic

import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.util.NSNotificationListener
import org.noiseplanet.noisecapture.util.toMicrophoneInfo
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.availableInputs
import platform.AVFAudio.currentRoute
import platform.Foundation.NSNotification


class IOSMicrophoneProvider : MicrophoneProvider(), KoinComponent {

    // - Properties

    private val audioSession = AVAudioSession.sharedInstance()
    private val inputsChangeListener = NSNotificationListener(
        notificationName = AVAudioSessionRouteChangeNotification,
        `object` = audioSession,
        callback = { handleRouteChange(it) }
    )


    // - Lifecycle

    init {
        inputsChangeListener.startListening()
    }


    // - Protected functions

    override fun getCurrentlyAvailableInputs(): List<MicrophoneInfo> {
        return audioSession.availableInputs
            ?.mapNotNull { it as? AVAudioSessionPortDescription }
            ?.map { it.toMicrophoneInfo() }
            .orEmpty()
    }

    override fun getDefaultInput(): MicrophoneInfo? {
        val currentInput = audioSession.currentRoute.inputs.firstOrNull() ?: return null

        return (currentInput as? AVAudioSessionPortDescription)?.toMicrophoneInfo()
    }


    // - Private functions

    private fun handleRouteChange(notification: NSNotification) {
        val userInfo = notification.userInfo ?: return
        val reason = userInfo[AVAudioSessionRouteChangeReasonKey] ?: return

        logger.debug("AVAudioSession route changed: $reason")
        refresh()
    }
}
