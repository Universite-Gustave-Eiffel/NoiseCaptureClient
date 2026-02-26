package org.noiseplanet.noisecapture.services.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.model.dao.MicrophoneType
import org.noiseplanet.noisecapture.util.NSNotificationListener
import org.noiseplanet.noisecapture.util.toMicrophoneInfo
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.availableInputs
import platform.AVFAudio.currentRoute
import platform.Foundation.NSNotification

class IOSMicrophoneProviderService : MicrophoneProviderService(), KoinComponent {

    // - Properties

    private val audioSession = AVAudioSession.Companion.sharedInstance()
    private val inputsChangeListener = NSNotificationListener(
        notificationName = AVAudioSessionRouteChangeNotification,
        `object` = audioSession,
        callback = { handleRouteChange(it) }
    )

    private val scope = CoroutineScope(Dispatchers.IO)


    // - Lifecycle

    init {
        inputsChangeListener.startListening()
        scope.launch { refresh() }
    }


    // - Protected functions

    override suspend fun getCurrentlyAvailableInputs(): List<MicrophoneInfo> {
        return audioSession.availableInputs
            ?.mapNotNull { it as? AVAudioSessionPortDescription }
            ?.map { it.toMicrophoneInfo() }
            ?.filterNot { it.type == MicrophoneType.UNKNOWN }
            .orEmpty()
            .ifEmpty {
                // If no compatible device is available, return a default builtin input source
                listOf(DEFAULT_MICROPHONE)
            }
    }

    override suspend fun getDefaultInput(): MicrophoneInfo? {
        // If available, return AVAudioSession's current route, otherwise default to
        // last available input source
        return audioSession.currentRoute.inputs.firstOrNull()
            ?.let { it as? AVAudioSessionPortDescription }
            ?.toMicrophoneInfo()
            ?: availableInputs.value.lastOrNull()
    }


    // - Private functions

    private fun handleRouteChange(notification: NSNotification) {
        val userInfo = notification.userInfo ?: return
        val reason = userInfo[AVAudioSessionRouteChangeReasonKey] ?: return

        logger.debug("AVAudioSession route changed: $reason")
        scope.launch { refresh() }
    }
}
