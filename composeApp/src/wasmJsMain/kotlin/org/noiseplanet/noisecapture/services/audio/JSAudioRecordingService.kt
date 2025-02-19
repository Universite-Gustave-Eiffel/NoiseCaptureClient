package org.noiseplanet.noisecapture.services.audio

import kotlinx.browser.window
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.MediaRecorder
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.url.URL
import org.w3c.files.Blob

class JSAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private var mediaRecorder: MediaRecorder? = null
    private var blob: Blob? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(
                video = false.toJsBoolean(),
                audio = true.toJsBoolean()
            )
        ).then { stream ->
            configureMediaRecorder(stream)
            blob = null
            mediaRecorder?.start()
            recordingStartListener?.onRecordingStart()
            stream
        }.catch { error ->
            logger.error("getUserMedia error during AudioRecorder init: $error")
            error
        }
    }

    override fun stopRecordingToFile() {
        mediaRecorder?.stop()
    }

    // - Private functions

    private fun configureMediaRecorder(stream: MediaStream) {
        mediaRecorder = MediaRecorder(stream)
        mediaRecorder?.ondataavailable = { event ->
            // Triggered after calling MediaRecorder::stop(). Event will contain the audio data.
            blob = event.data
        }
        mediaRecorder?.onstop = {
            // Triggered after calling MediaRecorder::stop() and after MediaRecorder::ondataavailable
            logger.debug("Recording stopped.")
            blob?.let {
                val url = URL.createObjectURL(it)
                logger.debug("Audio URL: $url")
                logger.debug("Blob size: ${it.size}")
                recordingStopListener?.onRecordingStop(url)

                // TODO: Once settled on a storage strategy, store the audio file somewhere (OPFS?)
                //       For now, just revoke URL.
                URL.revokeObjectURL(url)
            } ?: logger.warning("Could not get recorder audio URL: Blob was null.")
        }
    }
}
