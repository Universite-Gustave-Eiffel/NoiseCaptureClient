package org.noiseplanet.noisecapture.services.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import java.io.File
import java.io.IOException


class AndroidAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val context: Context by inject()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFileUrl: String? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        logger.debug("Recording to $outputFileName")

        val filesDir = context.getExternalFilesDir(null)
        outputFileUrl = "${filesDir?.absolutePath}/$outputFileName.mp3"
        logger.debug("Files dir: $filesDir")

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
            setAudioSamplingRate(41_000)
            setOutputFile(outputFileUrl)

            // Finalise initialisation
            try {
                prepare()
            } catch (error: IOException) {
                logger.error("Error while setting up MediaRecorder", error)
            }
            logger.debug("MediaRecorder ready")

            // Start recording
            start()
            recordingStartListener?.onRecordingStart()
            logger.debug("Started recording!")
        }
    }

    override fun stopRecordingToFile() {
        // Stop recording and release recorder
        mediaRecorder?.apply {
            stop()
            release()
            outputFileUrl?.let {
                recordingStopListener?.onRecordingStop(it)
            }
        }
        // Drop reference
        mediaRecorder = null
        logger.debug("Stopped recording")
    }

    override fun getFileSize(audioUrl: String): Long? {
        val file = File(audioUrl)
        if (file.exists()) {
            return file.length()
        }
        return null
    }

    override fun deleteFileAtUrl(audioUrl: String) {
        val file = File(audioUrl)
        if (file.exists()) {
            file.delete()
        }
    }
}
