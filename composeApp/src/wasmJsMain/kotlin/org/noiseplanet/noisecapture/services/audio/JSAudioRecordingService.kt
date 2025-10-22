package org.noiseplanet.noisecapture.services.audio

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.interop.MediaRecorder
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.files.Blob
import org.w3c.files.File

@OptIn(ExperimentalWasmJsInterop::class)
class JSAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val audioStorageService: AudioStorageService by inject()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var mediaRecorder: MediaRecorder? = null
    private var blob: Blob? = null
    private var fileName: String? = null


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

    override suspend fun getFileSize(audioUrl: String): Long? {
        val (fileHandle, _) = OPFSHelper.getFileHandle(audioUrl) ?: return null
        val file: File = fileHandle.getFile().await()

        return file.size.toInt().toLong()
    }

    override fun deleteFileAtUrl(audioUrl: String) {
        scope.launch {
            val (fileHandle, directoryHandle) = OPFSHelper.getFileHandle(audioUrl) ?: return@launch
            directoryHandle.removeEntry(fileHandle.name).await()
        }
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
                    audioStorageService.store(key = url, blob = it)
                    recordingStopListener?.onRecordingStop(url)
                }
            } ?: logger.warning("Could not get recorder audio URL: Blob was null.")
        }
    }
}
