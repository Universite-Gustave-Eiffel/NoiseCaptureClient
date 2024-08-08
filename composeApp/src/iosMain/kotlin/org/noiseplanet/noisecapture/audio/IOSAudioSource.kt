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
import org.noiseplanet.noisecapture.log.Logger
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioTime
import platform.AVFAudio.sampleRate
import platform.AVFAudio.setActive
import platform.AVFAudio.setPreferredIOBufferDuration
import platform.AVFAudio.setPreferredSampleRate
import platform.Foundation.NSError
import platform.Foundation.NSTimeInterval
import platform.posix.uint32_t

/**
 * iOS [AudioSource] implementation using [AVAudioEngine]
 *
 * [Swift documentation](https://developer.apple.com/documentation/avfaudio/avaudioengine)
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IOSAudioSource(
    private val logger: Logger,
) : AudioSource {

    companion object {

        const val SAMPLES_BUFFER_SIZE: uint32_t = 1024u
    }

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val audioSession = AVAudioSession.sharedInstance()
    private val audioEngine = AVAudioEngine()


    override suspend fun setup(): Flow<AudioSamples> {
        try {
            setupAudioSession()
        } catch (e: IllegalStateException) {
            logger.error("Error during audio source setup", e)
        }

        val inputNode = audioEngine.inputNode
        val busNumber: ULong = 0u // Mono input

        inputNode.installTapOnBus(
            bus = busNumber,
            bufferSize = SAMPLES_BUFFER_SIZE,
            format = inputNode.outputFormatForBus(busNumber),
        ) { buffer, audioTime ->
            try {
                processBuffer(buffer, audioTime)
            } catch (e: IllegalArgumentException) {
                logger.warning("Wrong buffer data received from AVAudioEngine. Skipping.", e)
            }
        }

        try {
            logger.debug("Starting AVAudioEngine...")
            memScoped {
                val error: ObjCObjectVar<NSError?> = alloc()
                audioEngine.startAndReturnError(error.ptr)
                checkNoError(error.value) { "Error while starting AVAudioEngine" }
            }
            logger.debug("AVAudioEngine is now running")
        } catch (e: IllegalStateException) {
            logger.error("Error setting up audio source", e)
            // TODO: Retry?
        }

        return audioSamplesChannel.receiveAsFlow()
    }

    override fun release() {
        audioEngine.stop()
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation {
        return AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
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

    private fun setupAudioSession() {
        logger.debug("Starting AVAudioSession...")

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()
            audioSession.setCategory(
                category = AVAudioSessionCategoryRecord,
                error = error.ptr
            )
            checkNoError(error.value) { "Error while setting AVAudioSession category" }

            val sampleRate = audioSession.sampleRate
            audioSession.setPreferredSampleRate(sampleRate, error.ptr)
            checkNoError(error.value) { "Error while setting AVAudioSession sample rate" }

            val bufferDuration: NSTimeInterval =
                1.0 / sampleRate * SAMPLES_BUFFER_SIZE.toDouble()
            audioSession.setPreferredIOBufferDuration(bufferDuration, error.ptr)
            checkNoError(error.value) { "Error while setting AVAudioSession buffer size" }

            // TODO: Figure out how to add an observer for NSNotification.Name.AVAudioSessionInterruption
            //       so we can listen to external AVAudioSession interruptions
            audioSession.setActive(
                active = true,
                error = error.ptr
            )
            checkNoError(error.value) { "Error while starting AVAudioSession" }
        }
        logger.debug("AVAudioSession is now active")
    }

    /**
     * Checks an optional [NSError] and if it's not null, throws an [IllegalStateException] with
     * a given message and the error's localized description
     *
     * @param error Optional [NSError]
     * @param lazyMessage Provided error message
     * @throws [IllegalStateException] If given [NSError] is not null.
     */
    private fun checkNoError(error: NSError?, lazyMessage: () -> String) {
        check(error == null) {
            "${lazyMessage()}: ${error?.localizedDescription}"
        }
    }
}
