package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.browser.window
import org.noiseplanet.noisecapture.interop.AudioContext
import org.noiseplanet.noisecapture.permission.DefaultPermissionDelegate
import org.noiseplanet.noisecapture.permission.Permission
import org.w3c.dom.mediacapture.MediaStreamConstraints


internal class AudioRecordPermissionDelegate : DefaultPermissionDelegate(
    permission = Permission.RECORD_AUDIO
) {
    // - Public functions

    override fun providePermission() {
        val audioContext = AudioContext()

        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(
                video = false.toJsBoolean(),
                audio = true.toJsBoolean()
            )
        ).then { stream ->
            // Try to create an audio stream, this will trigger the audio permissions popup
            audioContext.createMediaStreamSource(stream)
            // Close this stream as we don't need it to stay open
            audioContext.close().then { void -> void }
            stream
        }.catch { error ->
            // If we can't get the audio stream, we consider it's because the user has
            // denied microphone access.
            error
        }
    }
}
