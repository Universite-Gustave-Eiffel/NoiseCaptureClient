package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.browser.window
import org.koin.core.logger.Level
import org.koin.mp.KoinPlatformTools
import org.noiseplanet.noisecapture.audio.AudioContext
import org.noiseplanet.noisecapture.permission.PermissionState
import org.w3c.dom.mediacapture.MediaStreamConstraints


class AudioRecordPermissionDelegate : PermissionDelegate {

    private val logger = KoinPlatformTools.defaultLogger(Level.DEBUG)
    private var permissionState = PermissionState.NOT_DETERMINED

    override fun getPermissionState(): PermissionState {
        // We can't directly check for permissions on all browsers so the permissions state
        // will be NOT_DETERMINED by default.
        // TODO: The problem with this approach is that we have to manually request permission
        //       everytime the user refreshes the page, even if it was already granted
        //       A better solution would be to store permission state in local storage or something
        return permissionState
    }

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
            permissionState = PermissionState.GRANTED
            // Close this stream as we don't need it to stay open
            audioContext.close().then { void ->
                logger.debug("Closed audio context")
                void
            }
            stream
        }.catch { error ->
            // If we can't get the audio stream, we consider it's because the user has
            // denied microphone access.
            permissionState = PermissionState.DENIED
            error
        }
    }

    override fun openSettingPage() {
        // TODO: Is there a common way to open browser settings?
        //       Should we just show an alert/popup inquiring users to manually grant permission?
    }
}
