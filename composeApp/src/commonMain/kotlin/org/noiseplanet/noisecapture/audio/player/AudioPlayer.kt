package org.noiseplanet.noisecapture.audio.player

import kotlin.time.Duration


/**
 * Shared interface for platform specific audio file player implementations.
 *
 * @param filePath Path to the file that should be played.
 */
abstract class AudioPlayer(
    val filePath: String,
) {
    // - Listeners

    /**
     * Will notify subscribers when this audio player is ready to play the audio clip.
     */
    interface OnPreparedListener {

        /**
         * Called when this audio player is ready to play the audio clip.
         */
        fun onPrepared()
    }

    /**
     * Will notify subscribers when audio clip has played until the end.
     */
    interface OnCompleteListener {

        /**
         * Called when this audio player has reached the end of the audio clip.
         */
        fun onComplete()
    }


    // - Properties

    /**
     * Duration of the audio clip.
     */
    abstract var duration: Duration

    /**
     * Current play head position.
     */
    abstract val currentPosition: Duration

    /**
     * True if audio is currently playing.
     */
    abstract val isPlaying: Boolean

    /**
     * Called when this audio player is ready to play the audio clip.
     */
    var onPreparedListener: OnPreparedListener? = null

    /**
     * Called when this audio player has reached the end of the audio clip.
     */
    var onCompleteListener: OnCompleteListener? = null


    // - Public functions

    /**
     * Initialises internal audio player asynchronously and loads in audio file.
     *
     * @throws FileNotFoundException Thrown if an error occurs during setup.
     * @throws IllegalStateException Thrown if an error occurs during setup.
     */
    abstract suspend fun prepare()

    /**
     * Starts audio playback from current play head position.
     */
    abstract fun play()

    /**
     * Pauses audio playback.
     */
    abstract fun pause()

    /**
     * Stops any currently playing audio and unloads audio file from memory.
     */
    abstract fun release()

    /**
     * Moves play head to a new position in the clip.
     */
    abstract fun seek(position: Duration)

    /**
     * Sets [onPreparedListener] with a given lambda.
     *
     * @param onPrepared Called when this audio player is ready to play the audio clip.
     */
    internal open fun setOnPreparedLister(onPrepared: () -> Unit) {
        onPreparedListener = object : OnPreparedListener {
            override fun onPrepared() {
                onPrepared()
            }
        }
    }

    /**
     * Sets [onCompleteListener] with a given lambda.
     *
     * @param onComplete Called when this audio player has reached the end of the audio clip.
     */
    internal open fun setOnCompleteLister(onComplete: () -> Unit) {
        onCompleteListener = object : OnCompleteListener {
            override fun onComplete() {
                onComplete()
            }
        }
    }
}
