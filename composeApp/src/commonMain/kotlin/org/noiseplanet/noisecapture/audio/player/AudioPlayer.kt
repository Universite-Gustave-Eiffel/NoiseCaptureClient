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
    // - Properties

    /**
     * Duration of the audio clip.
     */
    abstract val duration: Duration

    /**
     * Current play head position.
     */
    abstract val currentPosition: Duration

    /**
     * True if audio is currently playing.
     */
    abstract val isPlaying: Boolean


    // - Public functions

    /**
     * Starts audio playback from current play head position.
     */
    abstract fun play()

    /**
     * Pauses audio playback.
     */
    abstract fun pause()

    /**
     * Moves play head to a new position in the clip.
     */
    abstract fun seek(position: Duration)
}
