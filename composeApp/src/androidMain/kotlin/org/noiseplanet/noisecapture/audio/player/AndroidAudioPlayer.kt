package org.noiseplanet.noisecapture.audio.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
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

    private val logger: Logger by injectLogger()
    private val context: Context by inject()

    private var mediaPlayer: MediaPlayer? = null

    override var duration: Duration = Duration.ZERO

    override val currentPosition: Duration
        get() = (mediaPlayer?.currentPosition ?: 0).toDuration(unit = DurationUnit.MILLISECONDS)

    override val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false


    // - AudioPlayer

    override suspend fun prepare() {
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
            setOnPreparedListener {
                // When media player is ready, set clip duration.
                this@AndroidAudioPlayer.duration =
                    duration.toDuration(unit = DurationUnit.MILLISECONDS)

                logger.debug("Audio player is ready.")
                onPreparedListener?.onPrepared()
            }
            setOnCompletionListener {
                logger.debug("Audio clip has reached the end")
                onCompleteListener?.onComplete()
            }
            try {
                setDataSource(context, uri)
                prepareAsync()
            } catch (err: java.io.FileNotFoundException) {
                throw IllegalStateException(err)
            } catch (err: IllegalArgumentException) {
                throw IllegalStateException(err)
            }
        }
    }

    override fun play() {
        logErrorIfUninitialized("Trying to play an uninitialized audio player.")
        mediaPlayer?.start()
    }

    override fun pause() {
        logErrorIfUninitialized("Trying to pause an uninitialized audio player.")
        mediaPlayer?.pause()
    }

    override fun seek(position: Duration) {
        logErrorIfUninitialized("Trying to seek on a uninitialized audio player.")
        mediaPlayer?.seekTo(position.inWholeMilliseconds.toInt())
    }

    override fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    // - Private functions

    private fun logErrorIfUninitialized(message: String) {
        if (mediaPlayer == null) {
            logger.error(message)
        }
    }
}
