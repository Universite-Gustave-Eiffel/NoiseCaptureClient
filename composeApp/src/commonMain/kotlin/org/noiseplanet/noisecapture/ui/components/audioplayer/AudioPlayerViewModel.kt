package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
        private val AUDIO_PLAYER_POSITION_REFRESH_RATE = 50L.toDuration(DurationUnit.MILLISECONDS)
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

    private val currentPositionFlow = MutableStateFlow(Duration.ZERO)
    val currentPosition: StateFlow<Duration> = currentPositionFlow

    private val isLoadingFlow = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = isLoadingFlow

    val duration: Duration
        get() = audioPlayer.duration


    // - Lifecycle

    init {
        audioPlayer.setOnPreparedLister {
            viewModelScope.launch {
                while (true) {
                    currentPositionFlow.emit(audioPlayer.currentPosition)
                    delay(AUDIO_PLAYER_POSITION_REFRESH_RATE)
                }
            }
            isLoadingFlow.tryEmit(false)
        }
        audioPlayer.setOnCompleteLister {
            // When playback has reached the end of the clip, go back to the beginning.
            audioPlayer.seek(Duration.ZERO)
            updateButtonViewModel()
        }
    }


    // - Public functions

    fun togglePlayPause() {
        if (audioPlayer.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
        updateButtonViewModel()
    }

    fun seek(position: Duration) {
        audioPlayer.seek(position)
        currentPositionFlow.tryEmit(position)
    }


    // - Private function

    fun updateButtonViewModel() {
        playPauseButtonViewModelFlow.tryEmit(
            IconButtonViewModel(
                icon = if (audioPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                style = ButtonStyle.SECONDARY,
            )
        )
    }
}
