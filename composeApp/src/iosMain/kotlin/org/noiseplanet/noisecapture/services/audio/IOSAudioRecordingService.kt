package org.noiseplanet.noisecapture.services.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.checkNoError
import org.noiseplanet.noisecapture.util.injectLogger
import platform.AVFAudio.AVAudioQuality
import platform.AVFAudio.AVAudioQualityMedium
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.AudioFormatID
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask


/**
 * iOS implementation of [AudioRecordingService] using [AVAudioRecorder].
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSAudioRecordingService : AudioRecordingService, KoinComponent {

    // - Constants

    companion object {

        private const val OUTPUT_AUDIO_FORMAT_ID: AudioFormatID = kAudioFormatMPEG4AAC
        private const val OUTPUT_AUDIO_SAMPLE_RATE: Int = 44_100
        private const val OUTPUT_AUDIO_QUALITY: AVAudioQuality = AVAudioQualityMedium
        private const val OUTPUT_AUDIO_CHANNEL_COUNT: Int = 1 // Mono recording
    }


    // - Properties

    private val logger: Logger by injectLogger()

    private var audioRecorder: AVAudioRecorder? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        logger.debug("Start recording to $outputFileName")

        // Get an URL pointing to the output file
        val documentsUrl = getDocumentsDirectory()
        val fileUrl = documentsUrl?.URLByAppendingPathComponent("$outputFileName.m4a")
        checkNotNull(fileUrl) { "Could not create URL for file with name $outputFileName" }

        logger.debug("Output file URL: ${fileUrl.absoluteString}")

        // Audio recorder settings specifying compression strategy and properties
        val settings: Map<Any?, *> = mapOf(
            AVFormatIDKey to OUTPUT_AUDIO_FORMAT_ID,
            AVSampleRateKey to OUTPUT_AUDIO_SAMPLE_RATE,
            AVNumberOfChannelsKey to OUTPUT_AUDIO_CHANNEL_COUNT,
            AVEncoderAudioQualityKey to OUTPUT_AUDIO_QUALITY,
        )

        // Initialize AVAudioRecorder instance with our settings and file URL
        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()

            audioRecorder = AVAudioRecorder(
                uRL = fileUrl,
                settings = settings,
                error = error.ptr
            )
            checkNoError(error.value) { "Error while setting up AVAudioRecorder" }
        }

        // Launch audio recording
        logger.debug("Starting recording...")
        audioRecorder?.record()
        logger.debug("Recording started!")
        recordingStartListener?.onRecordingStart()
    }

    override fun stopRecordingToFile() {
        // Stop recording
        logger.debug("Stopping recording...")
        audioRecorder?.stop()
        logger.debug("Recording stopped")
        audioRecorder?.url?.absoluteString?.let {
            recordingStopListener?.onRecordingStop(it)
        }

        // Drop recorder reference
        audioRecorder = null
    }


    // - Private functions

    /**
     * Gets an [NSURL] pointing to the app's documents directory.
     *
     * @return URL to documents directory or nil if not found or not accessible.
     */
    private fun getDocumentsDirectory(): NSURL? {
        val urls = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        return urls.firstOrNull() as? NSURL?
    }
}
