package org.noiseplanet.noisecapture.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.MicrophoneLocation
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * Android audio source implementation
 *
 * @param context Android [Context] instance
 */
internal class AndroidAudioSource(
    private val context: Context,
) : AudioSource, KoinComponent {

    // - Properties

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var audioRecorder: AudioRecorder? = null
    private var audioSourceService: AudioSourceService? = null

    private val serviceConnection = object : ServiceConnection {

        /**
         * Called when service is connected through [Context.bindService].
         * Retrieves the service instance from the given [binder] and launches
         * audio recording.
         */
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            checkNotNull(binder) { "Binder is null" }

            val localBinder = binder as AudioSourceService.LocalBinder
            audioSourceService = localBinder.getService()

            val audioRecorder = audioRecorder
            checkNotNull(audioRecorder) { "Audio source was not properly initialized" }
            audioSourceService?.startRecording(audioRecorder)

            // Update state to notify UI that recording has started
            state = AudioSourceState.RUNNING
        }

        /**
         * Called when service is disconnected.
         */
        override fun onServiceDisconnected(name: ComponentName?) {
            audioSourceService = null
        }
    }

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
                startAudioSourceService()
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
                // Stops recording through the current service and update state to notify UI
                audioSourceService?.stopRecording()
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

        audioSourceService?.stopRecording()
        audioRecorder = null
        state = AudioSourceState.UNINITIALIZED
    }

    override fun getMicrophoneLocation(): MicrophoneLocation {
        return MicrophoneLocation.LOCATION_UNKNOWN
    }


    // - Private functions

    /**
     * Based on the current OS version, start the [AudioSourceService] as a foreground service
     * with a persistent notification, or as a "regular" service.
     */
    private fun startAudioSourceService() {
        val intent = Intent(context, AudioSourceService::class.java)

        // Based
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        context.bindService(intent, serviceConnection, 0)
    }
}
