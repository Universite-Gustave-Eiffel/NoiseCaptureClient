package org.noiseplanet.noisecapture.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.util.NSNotificationListener
import org.noiseplanet.noisecapture.util.getInput
import org.noiseplanet.noisecapture.util.runCatchingNSError
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionInterruptionReasonKey
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeEnded
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioTime
import platform.AVFAudio.sampleRate
import platform.AVFAudio.setActive
import platform.AVFAudio.setPreferredIOBufferDuration
import platform.AVFAudio.setPreferredSampleRate
import platform.Foundation.NSNotification
import platform.Foundation.NSTimeInterval
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * iOS [AudioSource] implementation using [AVAudioEngine]
 *
 * [Swift documentation](https://developer.apple.com/documentation/avfaudio/avaudioengine)
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalTime::class)
internal class IOSAudioSource : AudioSource(), KoinComponent {

    // - Constants

    companion object {

        // 125ms target buffer duration
        const val SAMPLES_BUFFER_DURATION: NSTimeInterval = 0.125
    }


    // - Properties

    private val audioSession = AVAudioSession.sharedInstance()
    private var audioEngine: AVAudioEngine? = null

    private val interruptionNotificationHandler = NSNotificationListener(
        notificationName = AVAudioSessionInterruptionNotification,
        `object` = audioSession,
        callback = { handleSessionInterruptionNotification(it) }
    )


    // - Public functions

    override suspend fun setupInternal() {
        setupAudioSession()
        setupAudioEngine()

        // Start listening to interruption notifications
        interruptionNotificationHandler.startListening()
    }

    override fun startInternal() {
        runCatchingNSError { nsError ->
            audioEngine?.startAndReturnError(nsError.ptr)
        }.getOrThrow()
    }

    override fun pauseInternal() {
        audioEngine?.stop()
    }

    override suspend fun releaseInternal() {
        // Stop and release audio engine...
        audioEngine?.stop()
        audioEngine = null
        // ... and stop audio session
        setAudioSessionActive(false)
        // Stop listening to interruption notifications
        interruptionNotificationHandler.stopListening()
    }


    // - Private functions

    /**
     * Setup underlying [AVAudioSession].
     *
     * @throws IllegalStateException if an error occurs during setup.
     */
    private fun setupAudioSession() {
        logger.debug("Initializing AVAudioSession...")

        runCatchingNSError { nsError ->
            audioSession.setCategory(
                category = AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeMeasurement,
                options = AVAudioSessionCategoryOptionMixWithOthers or AVAudioSessionCategoryOptionDefaultToSpeaker,
                error = nsError.ptr,
            )

            // Disable potential vibrations, haptics or notification sounds while recording
            audioSession.setAllowHapticsAndSystemSoundsDuringRecording(false, nsError.ptr)
            // Disable system interruption of recording (we want to do this ourselves
            // when judged necessary in order to cleanly stop the eventual ongoing measurement)
            audioSession.setPrefersNoInterruptionsFromSystemAlerts(false, nsError.ptr)

            val sampleRate = audioSession.sampleRate
            audioSession.setPreferredSampleRate(sampleRate, nsError.ptr)

            audioSession.setPreferredIOBufferDuration(SAMPLES_BUFFER_DURATION, nsError.ptr)

            runCatchingNSError { nsError ->
                // Set preferred input source (if any)
                microphoneProvider.preferredInput.value
                    ?.let { audioSession.getInput(it.id) }
                    ?.let { audioSession.setPreferredInput(it, nsError.ptr) }
            }

        }.onFailure {
            logger.error("Error while setting up audio session", it)
        }.onSuccess {
            setAudioSessionActive(true)
            logger.debug("AVAudioSession initialized")
        }
    }

    /**
     * Setup underlying [AVAudioEngine] with the current [AVAudioSession].
     *
     * @throws IllegalStateException if an error occurs during setup.
     */
    private fun setupAudioEngine() {
        val audioEngine = AVAudioEngine()
        val inputNode = audioEngine.inputNode
        val busNumber: ULong = 0u // Mono input
        val inputFormat = inputNode.inputFormatForBus(busNumber)
        val bufferSize = (audioSession.sampleRate * SAMPLES_BUFFER_DURATION).toUInt()

        inputNode.installTapOnBus(
            bus = busNumber,
            bufferSize = bufferSize,
            format = inputFormat,
        ) { buffer, audioTime ->
            try {
                processBuffer(buffer, audioTime)
            } catch (e: IllegalArgumentException) {
                logger.warning("Wrong buffer data received from AVAudioEngine. Skipping.", e)
            }
        }

        // Make sure voice processing and automatic gain staging are disabled
        runCatchingNSError { nsError ->
            inputNode.voiceProcessingAGCEnabled = false
            inputNode.setVoiceProcessingEnabled(false, nsError.ptr)
        }

        logger.debug("AVAudioEngine is now ready to receive incoming audio samples")

        // Keep a reference to audio engine to be able to stop it afterwards
        this.audioEngine = audioEngine
    }

    /**
     * Starts or stops the shared audio session.
     *
     * @param isActive True to start session, false to stop.
     *
     * @throws IllegalStateException if an error occurred while starting or stopping audio source
     */
    private fun setAudioSessionActive(isActive: Boolean) {
        runCatchingNSError { nsError ->
            audioSession.setActive(
                active = isActive,
                error = nsError.ptr
            )
        }.onFailure {
            logger.error(
                if (isActive) {
                    "Error while starting AVAudioSession"
                } else {
                    "Error while stopping AVAudioSession"
                },
                it
            )
        }
    }

    /**
     * Handles audio session interruption notification.
     * [Swift documentation](https://developer.apple.com/documentation/avfaudio/handling_audio_interruptions)
     *
     * @param notification Interruption notification body.
     */
    private fun handleSessionInterruptionNotification(notification: NSNotification) {
        // Extract underlying variables from NSNotification object
        val userInfo = notification.userInfo ?: return
        val typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? Long ?: return

        when (typeValue.toULong()) {
            AVAudioSessionInterruptionTypeBegan -> {
                logger.debug("Received audio interruption notification")

                val reason = userInfo[AVAudioSessionInterruptionReasonKey] as? Long ?: return
                logger.debug("Reason: $reason")
                pause()
            }

            AVAudioSessionInterruptionTypeEnded -> {
                logger.debug("Received end of audio interruption notification")

                val options = userInfo[AVAudioSessionInterruptionOptionKey] as? Long ?: return
                if (options.toULong() == AVAudioSessionInterruptionOptionShouldResume) {
                    logger.debug("Resuming recording")
                    start()

                    // TODO: Audio session is not restarting even if shouldResume is true
                    //       Do we need to start a new session? Restart audio engine?
                }
            }
        }
    }

    /**
     * Process incoming audio buffer from [AVAudioEngine]
     *
     * @param buffer PCM audio buffer. [Apple docs](https://developer.apple.com/documentation/avfaudio/avaudiopcmbuffer/).
     * @param audioTime Audio time object. [Apple docs](https://developer.apple.com/documentation/avfaudio/avaudiotime/).
     *
     * @throws IllegalStateException Thrown if the incoming data doesn't conform to what
     *                               is expected by the shared audio code.
     */
    private fun processBuffer(buffer: AVAudioPCMBuffer?, audioTime: AVAudioTime?) {
        requireNotNull(buffer) { "Null buffer received" }
        requireNotNull(audioTime) { "Null audio time receiver" }

        // Buffer size provided to audio engine is a request but not a guarantee
        val actualSamplesCount = buffer.frameLength.toInt()

        buffer.floatChannelData?.let { channelData ->
            // Convert native float buffer to a Kotlin FloatArray
            val samplesBuffer = FloatArray(actualSamplesCount) { index ->
                // Channel data is internally a pointer to a float array
                // so we need to go through pointed.value to access the actual
                // array and retrieve the element using index
                channelData.pointed.value?.get(index) ?: 0f
            }
            val timestamp = Clock.System.now().toEpochMilliseconds()

            // Send processed audio samples through Channel
            emitAudioSamples(
                AudioSamples(
                    timestamp = timestamp,
                    samplesBuffer,
                    audioTime.sampleRate.toInt(),
                )
            )
        }
    }
}
