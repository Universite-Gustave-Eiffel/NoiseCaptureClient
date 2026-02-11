package org.noiseplanet.noisecapture.services.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.util.getInputDevice
import org.noiseplanet.noisecapture.util.injectLogger
import java.io.File
import java.io.IOException


class AndroidAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val context: Context by inject()
    private val fileSystemService: FileSystemService by inject()
    private val microphoneProvider: MicrophoneProvider by inject()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        logger.debug("Recording to $outputFileName")

        val relativePath = "measurement/audio/$outputFileName.mp3"
        val absolutePath = fileSystemService.getAbsolutePath(relativePath) ?: return
        // Create parent directories if needed
        File(absolutePath).parentFile?.mkdirs()

        // Initialize media recorder for given output file name
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            // Configure MediaRecorder instance
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            setAudioSamplingRate(44_100)
            setOutputFile(absolutePath)

            // If preferred device is specified and available, use it as input source
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                microphoneProvider.activeDevice.value?.id?.toIntOrNull()?.let { deviceId ->
                    val deviceInfo = audioManager.getInputDevice(deviceId)
                    setPreferredDevice(deviceInfo)
                }
            }

            // Finalise initialisation
            try {
                prepare()
            } catch (error: IOException) {
                logger.error("Error while setting up MediaRecorder", error)
                return
            }
            logger.debug("MediaRecorder ready")

            // Start recording
            try {
                start()
                // Remember file relative path for when recording ends
                outputFile = File(relativePath)
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
                recordingStopListener?.onRecordingStop(it.path)
            }
        }
        // Drop reference
        mediaRecorder = null
        outputFile = null
        logger.debug("Stopped recording")
    }
}
