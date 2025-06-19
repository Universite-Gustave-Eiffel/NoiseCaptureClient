package org.noiseplanet.noisecapture.audio.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Android implementation of the [AudioPlayer] interface.
 */
class AndroidAudioPlayer(
    filePath: String,
) : AudioPlayer(filePath), KoinComponent {

    // - Properties

    private val mediaPlayer: MediaPlayer
    private val context: Context by inject()

    override val duration: Duration

    override val currentPosition: Duration
        get() = mediaPlayer.currentPosition.toDuration(unit = DurationUnit.MILLISECONDS)

    override val isPlaying: Boolean
        get() = mediaPlayer.isPlaying


    // - Lifecycle

    init {
        // Initialise media player and get clip duration.
        val file = File(filePath)
        val uri = Uri.fromFile(file)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, uri)
            prepare()
        }
        duration = mediaPlayer.duration.toDuration(unit = DurationUnit.MILLISECONDS)
    }


    // - AudioPlayer

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.stop()
    }

    override fun seek(position: Duration) {
        mediaPlayer.seekTo(position.inWholeMilliseconds.toInt())
    }
}
