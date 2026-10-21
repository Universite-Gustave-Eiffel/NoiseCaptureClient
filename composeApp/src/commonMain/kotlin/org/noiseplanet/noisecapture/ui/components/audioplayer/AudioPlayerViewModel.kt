package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.delete
import noisecapture.composeapp.generated.resources.details_delete_measurement_audio_dialog_text
import noisecapture.composeapp.generated.resources.details_delete_measurement_dialog_title
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.audio.player.AudioPlayer
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialogViewModel
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AudioPlayerViewModel(
    val measurement: Measurement,
) : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        /**
         * Tells how often the play head position is refreshed in our view.
         */
        private val AUDIO_PLAYER_POSITION_REFRESH_RATE = 50L.toDuration(DurationUnit.MILLISECONDS)
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val audioPlayer: AudioPlayer by inject { parametersOf(measurement.recordedAudioUrl) }
    private val measurementService: MeasurementService by inject()

    private val isPlayingFlow = MutableStateFlow(audioPlayer.isPlaying)
    val isPlaying: StateFlow<Boolean> = isPlayingFlow

    private val currentPositionFlow = MutableStateFlow(Duration.ZERO)
    val currentPosition: StateFlow<Duration> = currentPositionFlow

    private val isReadyFlow = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = isReadyFlow

    val duration: Duration
        get() = audioPlayer.duration

    val deleteConfirmationDialogViewModel: NCDialogViewModel = NCDialogViewModel(
        title = Res.string.details_delete_measurement_dialog_title,
        text = Res.string.details_delete_measurement_audio_dialog_text,
        confirmButtonContent = ButtonContent(title = Res.string.delete),
        confirmButtonColors = Color.Noise.eight.secondaryContainerColors(hasDropShadow = true),
        dismissButtonContent = ButtonContent(title = Res.string.cancel),
        dismissButtonColors = Color.Noise.one.transparentContainerColors(),
    )


    // - Lifecycle

    init {
        audioPlayer.setOnPreparedLister {
            viewModelScope.launch {
                while (true) {
                    currentPositionFlow.emit(audioPlayer.currentPosition)
                    delay(AUDIO_PLAYER_POSITION_REFRESH_RATE)
                }
            }
            isReadyFlow.tryEmit(true)
        }
        audioPlayer.setOnCompleteLister {
            // When playback has reached the end of the clip, go back to the beginning.
            audioPlayer.seek(Duration.ZERO)
            isPlayingFlow.tryEmit(false)
        }
        viewModelScope.launch {
            try {
                audioPlayer.prepare()
            } catch (err: IllegalStateException) {
                logger.error("Error while setting up audio player", err)
            }
        }
    }


    // - Public functions

    fun togglePlayPause() {
        if (audioPlayer.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
        isPlayingFlow.tryEmit(audioPlayer.isPlaying)
    }

    fun seek(position: Duration) {
        audioPlayer.seek(position)
        currentPositionFlow.tryEmit(position)
    }

    fun release() {
        audioPlayer.release()
    }

    fun deleteAudioClip() {
        viewModelScope.launch {
            measurementService.deleteMeasurementAssociatedAudio(measurement)
        }
    }
}
