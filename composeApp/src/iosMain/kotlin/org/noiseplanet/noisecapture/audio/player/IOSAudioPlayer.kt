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
import org.noiseplanet.noisecapture.util.checkNoError
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

    private val audioPlayer: AVAudioPlayer
    private val delegate = AVAudioPlayerDelegate(
        onComplete = { onCompleteListener?.onComplete() }
    )

    override var duration: Duration

    override val currentPosition: Duration
        get() = audioPlayer.currentTime.toDuration(unit = DurationUnit.SECONDS)

    override val isPlaying: Boolean
        get() = audioPlayer.isPlaying()


    // - Lifecycle

    init {
        val url = requireNotNull(NSURL.URLWithString(filePath))

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()

            // Setup audio player
            audioPlayer = AVAudioPlayer(
                contentsOfURL = url,
                error = error.ptr,
            )
            audioPlayer.prepareToPlay()

            checkNoError(error.value) { "Error while setting up AVAudioPlayer" }
        }
        audioPlayer.delegate = delegate
        duration = audioPlayer.duration.toDuration(unit = DurationUnit.SECONDS)

        onPreparedListener?.onPrepared()
    }


    // - AudioPlayer

    override fun play() {
        audioPlayer.play()
    }

    override fun pause() {
        audioPlayer.pause()
    }

    override fun seek(position: Duration) {
        val timeInterval: NSTimeInterval = position.inMs / 1_000.0
        audioPlayer.currentTime = timeInterval
    }

    override fun setOnPreparedLister(onPrepared: () -> Unit) {
        super.setOnPreparedLister(onPrepared)

        // iOS Audio player is prepared synchronously as soon as it is initialized.
        onPrepared()
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
