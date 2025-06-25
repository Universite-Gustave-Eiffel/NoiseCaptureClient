package org.noiseplanet.noisecapture.audio.player

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.koin.core.component.KoinComponent
import org.koin.core.time.inMs
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.checkNoError
import org.noiseplanet.noisecapture.util.injectLogger
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.Foundation.NSError
import platform.Foundation.NSTimeInterval
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


/**
 * iOS implementation of the [AudioPlayer] interface.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSAudioPlayer(filePath: String) : AudioPlayer(filePath), KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private var audioPlayer: AVAudioPlayer? = null
    private val delegate = AVAudioPlayerDelegate(
        onComplete = { onCompleteListener?.onComplete() }
    )

    override var duration: Duration = Duration.ZERO

    override val currentPosition: Duration
        get() = (audioPlayer?.currentTime ?: 0.0).toDuration(unit = DurationUnit.SECONDS)

    override val isPlaying: Boolean
        get() = audioPlayer?.isPlaying() ?: false


    // - AudioPlayer

    override suspend fun prepare() {
        val url = requireNotNull(NSURL.URLWithString(filePath))

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()

            // Setup audio player
            audioPlayer = AVAudioPlayer(
                contentsOfURL = url,
                error = error.ptr,
            )
            audioPlayer?.prepareToPlay()

            checkNoError(error.value) { "Error while setting up AVAudioPlayer" }
        }
        val audioPlayer = checkNotNull(audioPlayer)
        audioPlayer.delegate = delegate
        duration = audioPlayer.duration.toDuration(unit = DurationUnit.SECONDS)

        onPreparedListener?.onPrepared()
    }

    override fun play() {
        logErrorIfUninitialized("Trying to play an uninitialized audio player.")
        audioPlayer?.play()
    }

    override fun pause() {
        logErrorIfUninitialized("Trying to pause an uninitialized audio player.")
        audioPlayer?.pause()
    }

    override fun seek(position: Duration) {
        logErrorIfUninitialized("Trying to seek on a uninitialized audio player.")
        val timeInterval: NSTimeInterval = position.inMs / 1_000.0
        audioPlayer?.currentTime = timeInterval
    }

    override fun release() {
        audioPlayer?.stop()
        audioPlayer = null
    }


    // - Private functions

    private fun logErrorIfUninitialized(message: String) {
        if (audioPlayer == null) {
            logger.error(message)
        }
    }
}


/**
 * Subscribe to updates from [AVAudioPlayer].
 */
private class AVAudioPlayerDelegate(
    private val onComplete: () -> Unit,
) : NSObject(), AVAudioPlayerDelegateProtocol {

    /**
     * Called when audio clip reaches the end of the stream.
     */
    override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
        onComplete()
    }
}
