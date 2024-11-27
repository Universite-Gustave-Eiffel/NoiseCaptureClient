package org.noiseplanet.noisecapture.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.MicrophoneLocation

/**
 * Android audio source implementation
 *
 * @param logger [Logger] instance
 * @param context Android [Context] instance
 */
internal class AndroidAudioSource(
    private val logger: Logger,
    private val context: Context,
) : AudioSource {

    // - Properties

    private var audioThread: Thread? = null
    private var audioRecorder: AudioRecorder? = null

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var audioForegroundService: AudioForegroundService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            logger.debug("ON SERVICE CONNECTED")
            checkNotNull(binder) { "Binder is null" }

            val localBinder = binder as AudioForegroundService.LocalBinder
            audioForegroundService = localBinder.getService()

            val audioRecorder = audioRecorder
            checkNotNull(audioRecorder) { "Audio source was not properly initialized" }
            audioForegroundService?.startRecording(audioRecorder)
            state = AudioSourceState.RUNNING
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logger.debug("ON SERVICE CONNECTED")
            audioForegroundService = null
        }
    }


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
        // Create a recorder that will process raw incoming audio into audio samples
        // and broadcast it through the channel.
        audioRecorder = AudioRecorder(audioSamplesChannel, logger)
        state = AudioSourceState.READY
    }

    override fun start() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.error("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Audio source already running.")
                return
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Starting audio source.")
                // Start a foreground service for recording incoming audio
                startForegroundService()
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
                audioForegroundService?.stopRecording()
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

        audioForegroundService?.stopRecording()
        audioRecorder = null
        audioThread = null
        state = AudioSourceState.UNINITIALIZED
    }

    override fun getMicrophoneLocation(): MicrophoneLocation {
        return MicrophoneLocation.LOCATION_UNKNOWN
    }


    // - Private functions

    private fun startForegroundService() {
        val intent = Intent(context, AudioForegroundService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, 0)
    }
}
