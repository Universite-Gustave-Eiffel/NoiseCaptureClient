package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.browser.window
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.AudioContext
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.checkPermission
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.dom.mediacapture.MediaStreamConstraints


class AudioRecordPermissionDelegate : PermissionDelegate, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private var permissionState = PermissionState.NOT_DETERMINED


    // - PermissionDelegate

    override suspend fun getPermissionState(): PermissionState {
        if (permissionState == PermissionState.GRANTED) {
            return permissionState
        }
        return checkPermission(Permission.RECORD_AUDIO)
    }

    override suspend fun providePermission() {
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
