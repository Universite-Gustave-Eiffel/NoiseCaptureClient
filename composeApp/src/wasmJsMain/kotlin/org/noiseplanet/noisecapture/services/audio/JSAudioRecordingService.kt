package org.noiseplanet.noisecapture.services.audio

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.interop.MediaRecorder
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.services.storage.OPFSFileSystemService
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.mediacapture.MediaTrackConstraints
import org.w3c.files.Blob

@OptIn(ExperimentalWasmJsInterop::class)
class JSAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val fileSystemService: FileSystemService by inject()
    private val microphoneProvider: MicrophoneProvider by inject()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var mediaRecorder: MediaRecorder? = null
    private var blob: Blob? = null
    private var fileName: String? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        // Setup audio track constraints (asking for no AGC, noise suppression, etc)
        val audioConstraints = MediaTrackConstraints(
            advanced = JsArray(), // Useless but required otherwise the constraints object fails to parse
            autoGainControl = false.toJsBoolean(),
            noiseSuppression = false.toJsBoolean(),
            echoCancellation = false.toJsBoolean(),
        )

        // If a preferred input source is available, add it as an additional constraint
        // Note: Depending on the browser, it may trigger an additional microphone permission popup.
        microphoneProvider.preferredInput.value?.let {
            logger.debug("Selected device: ${it.label} (ID: ${it.id})")
            audioConstraints.deviceId = it.id.toJsString()
        }

        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(audio = audioConstraints)
        ).then { stream ->
            configureMediaRecorder(stream)
            blob = null
            fileName = outputFileName
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
                val url = "measurement/audio/$fileName.ogg"
                scope.launch {
                    (fileSystemService as? OPFSFileSystemService)?.store(key = url, blob = it)
                    recordingStopListener?.onRecordingStop(url)
                }
            } ?: logger.warning("Could not get recorder audio URL: Blob was null.")
        }
    }
}
