package org.noise_planet.noisecapture

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints

const val SAMPLES_REPLAY = 10
const val SAMPLES_CACHE = 10
const val BUFFER_SIZE_TIME = 0.1

class JsAudioSource : AudioSource {
    override val samples = MutableSharedFlow<AudioSamples>(replay = SAMPLES_REPLAY,
        extraBufferCapacity = SAMPLES_CACHE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun setup(): AudioSource.InitializeErrorCode {
        var code : AudioSource.InitializeErrorCode = AudioSource.InitializeErrorCode.INITIALIZE_ALREADY_INITIALIZED
        console.log("fetch user media..")
        suspend {
            window.navigator.mediaDevices.getUserMedia(MediaStreamConstraints(audio = true)).then {
                mediaStream ->
                val audioTracks = mediaStream.getAudioTracks()
                console.log(audioTracks)
                if(audioTracks.isNotEmpty()) {
                    code = AudioSource.InitializeErrorCode.INITIALIZE_OK
                } else {
                    code = AudioSource.InitializeErrorCode.INITIALIZE_NO_MICROPHONE
                }
            }.catch { throwable ->
                console.log("Error accessing the microphone", throwable)
                code = AudioSource.InitializeErrorCode.INITIALIZE_NO_MICROPHONE
            }.await()
        }
        return code
    }

    override fun getSampleRate(): Int = 48000

    override fun release() {
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}
