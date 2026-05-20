package org.noiseplanet.noisecapture.services.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.io.files.SystemFileSystem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.runCatchingNSError
import platform.AVFAudio.AVAudioQuality
import platform.AVFAudio.AVAudioQualityMedium
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionChannelDescription
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.currentRoute
import platform.CoreAudioTypes.AudioFormatID
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSURL


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
    private val fileSystemService: FileSystemService by inject()

    private val audioSession = AVAudioSession.sharedInstance()
    private var audioRecorder: AVAudioRecorder? = null
    private var recordingUrl: String? = null


    // - AudioRecordingService

    override var recordingStartListener: AudioRecordingService.RecordingStartListener? = null
    override var recordingStopListener: AudioRecordingService.RecordingStopListener? = null

    override fun startRecordingToFile(outputFileName: String) {
        logger.debug("Start recording to $outputFileName")

        // Get an URL pointing to the output file
        val relativePath = "measurement/audio/$outputFileName.m4a"
        val absolutePath = fileSystemService.getAbsolutePath(relativePath)
        val fileUri = absolutePath?.let { NSURL.URLWithString(it.toString()) }
        checkNotNull(fileUri) { "Could not create URL for file with name $outputFileName" }
        logger.debug("Output file URL: $fileUri")

        // Create enclosing directories if needed
        absolutePath.parent?.let { SystemFileSystem.createDirectories(it) }

        // Audio recorder settings specifying compression strategy and properties
        val settings: Map<Any?, *> = mapOf(
            AVFormatIDKey to OUTPUT_AUDIO_FORMAT_ID,
            AVSampleRateKey to OUTPUT_AUDIO_SAMPLE_RATE,
            AVNumberOfChannelsKey to OUTPUT_AUDIO_CHANNEL_COUNT,
            AVEncoderAudioQualityKey to OUTPUT_AUDIO_QUALITY,
        )

        // Initialize AVAudioRecorder instance with our settings and file URL
        audioRecorder = runCatchingNSError { nsError ->
            AVAudioRecorder(
                uRL = fileUri,
                settings = settings,
                error = nsError.ptr
            ).apply {
                // Set preferred input if possible
                // https://developer.apple.com/documentation/avfaudio/routing-audio-to-specific-devices-in-multidevice-sessions#Route-high-level-audio-an-audio-player-or-recorder
                audioSession.currentRoute.inputs.map { it as? AVAudioSessionPortDescription }
                    .firstOrNull()?.channels
                    ?.map { it as? AVAudioSessionChannelDescription }
                    ?.firstOrNull()
                    ?.let { setChannelAssignments(listOf(it)) }
            }
        }.onSuccess { audioRecorder ->
            // Launch audio recording
            logger.debug("Starting recording...")
            audioRecorder.record()
            recordingUrl = relativePath
            logger.debug("Recording started!")
            recordingStartListener?.onRecordingStart()
        }.onFailure {
            logger.error("Error while setting up AVAudioRecorder", it)
        }.getOrNull()
    }

    override fun stopRecordingToFile() {
        // Stop recording
        logger.debug("Stopping recording...")
        audioRecorder?.stop()
        logger.debug("Recording stopped")
        recordingUrl?.let {
            recordingStopListener?.onRecordingStop(it)
        }

        // Drop recorder reference
        audioRecorder = null
    }
}
