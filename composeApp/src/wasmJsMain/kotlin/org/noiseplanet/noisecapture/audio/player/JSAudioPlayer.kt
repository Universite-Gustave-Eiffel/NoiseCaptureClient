package org.noiseplanet.noisecapture.audio.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.time.inMs
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.dom.Audio
import org.w3c.dom.url.URL
import org.w3c.files.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class JSAudioPlayer(filePath: String) : AudioPlayer(filePath), KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val scope = CoroutineScope(Dispatchers.Default)

    private var audioElement: Audio? = null

    override var duration: Duration = Duration.ZERO

    override val currentPosition: Duration
        get() = audioElement?.currentTime?.toDuration(unit = DurationUnit.SECONDS) ?: Duration.ZERO

    override val isPlaying: Boolean
        get() = audioElement?.paused?.not() ?: false


    // - Lifecycle

    init {
        scope.launch {
            val (fileHandle, _) = OPFSHelper.getFileHandle(filePath) ?: return@launch
            val file: File = fileHandle.getFile().await()

            val blobURL = URL.createObjectURL(file)
            logger.debug("Created blob from OPFS file: $blobURL")

            audioElement = Audio(blobURL).apply {
                addEventListener("ended") {
                    logger.debug("Audio clip ended.")
                    this@JSAudioPlayer.onCompleteListener?.onComplete()
                }
                addEventListener("loadedmetadata") {
                    logger.debug("Loaded audio clip. Duration: $duration seconds")
                    this@JSAudioPlayer.duration = duration.toDuration(unit = DurationUnit.SECONDS)
                    this@JSAudioPlayer.onPreparedListener?.onPrepared()
                }
                load()
            }
        }
    }


    // - AudioPlayer

    override fun play() {
        if (audioElement == null) {
            logger.error("Trying to play an uninitialized audio element.")
            return
        }
        audioElement?.play()
    }

    override fun pause() {
        if (audioElement == null) {
            logger.error("Trying to pause an uninitialized audio element.")
            return
        }
        audioElement?.pause()
    }

    override fun seek(position: Duration) {
        if (audioElement == null) {
            logger.error("Trying to seek an uninitialized audio element.")
            return
        }
        audioElement?.currentTime = position.inMs / 1_000
    }
}
