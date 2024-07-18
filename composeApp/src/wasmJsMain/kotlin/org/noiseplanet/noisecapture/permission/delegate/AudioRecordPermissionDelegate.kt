package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.browser.window
import org.noiseplanet.noisecapture.audio.AudioContext
import org.noiseplanet.noisecapture.permission.PermissionState
import org.w3c.dom.mediacapture.MediaStreamConstraints


@JsFun(
    """
        navigator.permissions.query({ name: "microphone" }).await().state.toString()
    """
)
external fun requestMicrophonePermission(): JsString


class AudioRecordPermissionDelegate : PermissionDelegate {

    private var overrideState = PermissionState.NOT_DETERMINED

    override fun getPermissionState(): PermissionState {
        if (overrideState == PermissionState.GRANTED) {
            return overrideState
        }
        return try {
            when (
                requestMicrophonePermission().toString()
            ) {
                "granted" -> PermissionState.GRANTED
                else -> PermissionState.DENIED
            }
        } catch (ignore: JsException) {
            PermissionState.NOT_DETERMINED
        }
    }

    override fun providePermission() {
        val audioContext = AudioContext()
        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(
                video = false.toJsBoolean(),
                audio = true.toJsBoolean()
            )
        ).then { stream ->
            audioContext.createMediaStreamSource(stream)
            overrideState = PermissionState.GRANTED

            Unit.toJsReference()
        }
    }

    override fun openSettingPage() {
    }
}
