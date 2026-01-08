package org.noiseplanet.noisecapture.services.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.util.injectLogger
import java.io.File
import java.io.IOException


class AndroidAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val context: Context by inject()
    private val fileSystemService: FileSystemService by inject()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        logger.debug("Recording to $outputFileName")

        val audioDir = fileSystemService.getAudioFilesDirectoryUri() ?: return
        val outputFileUri = "$audioDir/$outputFileName.mp3"
        outputFile = File(outputFileUri)

        // Initialize media recorder for given output file name
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(get())
        } else {
            MediaRecorder()
        }.apply {
            // Configure MediaRecorder instance
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            setAudioSamplingRate(44_100)
            setOutputFile(outputFileUri)

            // Finalise initialisation
            try {
                prepare()
            } catch (error: IOException) {
                logger.error("Error while setting up MediaRecorder", error)
            }
            logger.debug("MediaRecorder ready")

            // Start recording
            try {
                start()
                recordingStartListener?.onRecordingStart()
                logger.debug("Started recording!")
            } catch (error: IllegalStateException) {
                logger.error("Error while starting audio recording", error)
            }
        }
    }

    override fun stopRecordingToFile() {
        // Stop recording and release recorder
        mediaRecorder?.apply {
            try {
                stop()
            } catch (error: IllegalStateException) {
                logger.error("Error while stopping audio recording", error)
            }
            release()
            outputFile?.let {
                recordingStopListener?.onRecordingStop(it.name)
            }
        }
        // Drop reference
        mediaRecorder = null
        logger.debug("Stopped recording")
    }
}
