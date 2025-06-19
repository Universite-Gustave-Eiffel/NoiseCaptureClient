package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.audio.player.AudioPlayer
import org.noiseplanet.noisecapture.ui.components.button.ButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.IconButtonViewModel
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AudioPlayerViewModel(
    val filePath: String,
) : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        /**
         * Tells how often the play head position is refreshed in our view.
         */
        private val AUDIO_PLAYER_POSITION_REFRESH_RATE = 500L.toDuration(DurationUnit.MILLISECONDS)
    }


    // - Properties

    private val audioPlayer: AudioPlayer by inject { parametersOf(filePath) }

    private val playPauseButtonViewModelFlow = MutableStateFlow(
        IconButtonViewModel(
            icon = Icons.Default.PlayArrow,
            style = ButtonStyle.SECONDARY,
        )
    )
    val playPauseButtonViewModel: StateFlow<IconButtonViewModel> = playPauseButtonViewModelFlow

    val currentPosition: StateFlow<Duration> = flow {
        emit(audioPlayer.currentPosition)
        delay(AUDIO_PLAYER_POSITION_REFRESH_RATE)
    }.stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Duration.ZERO,
    )

    val duration: Duration = audioPlayer.duration


    // - Public functions

    fun togglePlayPause() {
        if (audioPlayer.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
        playPauseButtonViewModelFlow.tryEmit(
            IconButtonViewModel(
                icon = if (audioPlayer.isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                style = ButtonStyle.SECONDARY,
            )
        )
    }

    fun seek(position: Duration) {
        audioPlayer.seek(position)
    }
}
