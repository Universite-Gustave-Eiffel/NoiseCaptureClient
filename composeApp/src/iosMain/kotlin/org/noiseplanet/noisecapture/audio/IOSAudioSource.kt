package org.noiseplanet.noisecapture.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.MicrophoneLocation
import org.noiseplanet.noisecapture.util.NSNotificationListener
import org.noiseplanet.noisecapture.util.checkNoError
import org.noiseplanet.noisecapture.util.injectLogger
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
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
import platform.Foundation.NSError
import platform.Foundation.NSNotification
import platform.Foundation.NSTimeInterval
import platform.posix.uint32_t

/**
 * iOS [AudioSource] implementation using [AVAudioEngine]
 *
 * [Swift documentation](https://developer.apple.com/documentation/avfaudio/avaudioengine)
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IOSAudioSource : AudioSource, KoinComponent {

    // - Constants

    companion object {

        const val SAMPLES_BUFFER_SIZE: uint32_t = 1024u
    }


    // - Properties

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val audioSession = AVAudioSession.sharedInstance()
    private var audioEngine: AVAudioEngine? = null

    private val interruptionNotificationHandler = NSNotificationListener(
        notificationName = AVAudioSessionInterruptionNotification,
        `object` = audioSession,
        callback = { handleSessionInterruptionNotification(it) }
    )

    private val logger: Logger by injectLogger()


    // - AudioSource

    override var state: AudioSourceState = AudioSourceState.UNINITIALIZED
        set(value) {
            field = value
            stateChannel.trySend(value)
        }

    override val audioSamples: Flow<AudioSamples> = audioSamplesChannel.receiveAsFlow()
    override val stateFlow: Flow<AudioSourceState> = stateChannel.receiveAsFlow()

    override fun setup() {
        if (state != AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source is already initialized, skipping setup.")
            return
        }

        try {
            setupAudioSession()
            setupAudioEngine()
        } catch (e: IllegalStateException) {
            logger.error("Error during audio source setup", e)
            state = AudioSourceState.UNINITIALIZED
            return
        }

        // Start listening to interruption notifications
        interruptionNotificationHandler.startListening()
        state = AudioSourceState.READY
    }

    override fun start() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.error("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Audio source already started.")
                return
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Starting audio source")
                memScoped {
                    val error: ObjCObjectVar<NSError?> = alloc()
                    audioEngine?.startAndReturnError(error.ptr)
                    checkNoError(error.value) { "Error while starting AVAudioEngine" }
                    state = AudioSourceState.RUNNING
                }
            }
        }
    }

    override fun pause() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.error("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Pausing audio source.")
                audioEngine?.stop()
                state = AudioSourceState.PAUSED
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Audio source already paused.")
                return
            }
        }
    }

    override fun release() {
        if (state == AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source already uninitialized, skipping cleanup.")
            return
        }

        // Stop and release audio engine...
        audioEngine?.stop()
        audioEngine = null
        // ... and stop audio session
        setAudioSessionActive(false)
        // Stop listening to interruption notifications
        interruptionNotificationHandler.stopListening()
        // Lastly, update state
        state = AudioSourceState.UNINITIALIZED
    }

    override fun getMicrophoneLocation(): MicrophoneLocation {
        return MicrophoneLocation.LOCATION_UNKNOWN
    }


    // - Private functions

    /**
     * Setup underlying [AVAudioSession].
     *
     * @throws IllegalStateException if an error occurs during setup.
     */
    private fun setupAudioSession() {
        logger.debug("Initializing AVAudioSession...")

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()

            audioSession.setCategory(
                category = AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeMeasurement,
                options = AVAudioSessionCategoryOptionMixWithOthers,
                error = error.ptr,
            )
            checkNoError(error.value) { "Error while setting AVAudioSession category" }

            val sampleRate = audioSession.sampleRate
            audioSession.setPreferredSampleRate(sampleRate, error.ptr)
            checkNoError(error.value) { "Error while setting AVAudioSession sample rate" }

            val bufferDuration: NSTimeInterval =
                1.0 / sampleRate * SAMPLES_BUFFER_SIZE.toDouble()
            audioSession.setPreferredIOBufferDuration(bufferDuration, error.ptr)
            checkNoError(error.value) { "Error while setting AVAudioSession buffer size" }
        }
        setAudioSessionActive(true)

        logger.debug("AVAudioSession initialized")
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

        inputNode.installTapOnBus(
            bus = busNumber,
            bufferSize = SAMPLES_BUFFER_SIZE,
            format = inputFormat,
        ) { buffer, audioTime ->
            try {
                processBuffer(buffer, audioTime)
            } catch (e: IllegalArgumentException) {
                logger.warning("Wrong buffer data received from AVAudioEngine. Skipping.", e)
            }
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
        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()
            audioSession.setActive(
                active = true,
                error = error.ptr
            )
            checkNoError(error.value) {
                if (isActive) {
                    "Error while starting AVAudioSession"
                } else {
                    "Error while stopping AVAudioSession"
                }
            }
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
                // TODO: Trigger delegate callback to update UI?

                val reason = userInfo[AVAudioSessionInterruptionReasonKey] as? Long ?: return
                logger.debug("Reason: $reason")
                pause()
            }

            AVAudioSessionInterruptionTypeEnded -> {
                logger.debug("Received end of audio interruption notification")
                // TODO: Trigger delegate callback to update UI?

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
            // Send processed audio samples through Channel
            audioSamplesChannel.trySend(
                AudioSamples(
                    audioTime.hostTime.toLong(),
                    samplesBuffer,
                    audioTime.sampleRate.toInt(),
                )
            )
        }
    }
}
