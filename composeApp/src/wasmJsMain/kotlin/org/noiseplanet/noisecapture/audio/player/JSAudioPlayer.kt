package org.noiseplanet.noisecapture.audio.player

import kotlinx.coroutines.await
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


@OptIn(ExperimentalWasmJsInterop::class)
class JSAudioPlayer(filePath: String) : AudioPlayer(filePath), KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private var audioElement: Audio? = null

    override var duration: Duration = Duration.ZERO

    override val currentPosition: Duration
        get() = audioElement?.currentTime?.toDuration(unit = DurationUnit.SECONDS) ?: Duration.ZERO

    override val isPlaying: Boolean
        get() = audioElement?.paused?.not() ?: false


    // - AudioPlayer

    override suspend fun prepare() {
        // Get audio file from path and create a blob url from it.
        val (fileHandle, _) = checkNotNull(OPFSHelper.getFileHandle(filePath)) {
            "Audio player could not find file $filePath"
        }
        val file: File = fileHandle.getFile().await()
        val blobURL = URL.createObjectURL(file)

        logger.debug("Created blob from OPFS file: $blobURL")

        // Initialize audio element
        audioElement = Audio(blobURL).apply {
            // Subscribe to listeners
            addEventListener("ended") {
                logger.debug("Audio clip ended.")
                onCompleteListener?.onComplete()
            }
            addEventListener("loadedmetadata") {
                logger.debug("Loaded audio clip. Duration: $duration seconds")

                this@JSAudioPlayer.duration = duration.toDuration(unit = DurationUnit.SECONDS)
                onPreparedListener?.onPrepared()
            }
            // Load audio file into memory
            load()
        }
    }

    override fun play() {
        logErrorIfUninitialized("Trying to play an uninitialized audio player.")
        audioElement?.play()
    }

    override fun pause() {
        logErrorIfUninitialized("Trying to pause an uninitialized audio player.")
        audioElement?.pause()
    }

    override fun seek(position: Duration) {
        logErrorIfUninitialized("Trying to seek on a uninitialized audio player.")
        audioElement?.currentTime = position.inMs / 1_000
    }

    override fun release() {
        audioElement?.src?.let { URL.revokeObjectURL(it) }
        audioElement = null
    }


    // - Private functions

    private fun logErrorIfUninitialized(message: String) {
        if (audioElement == null) {
            logger.error(message)
        }
    }
}
