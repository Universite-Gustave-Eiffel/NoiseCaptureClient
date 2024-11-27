package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.services.LiveAudioService
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class MeasurementScreenViewModel(
    private val liveAudioService: LiveAudioService,
) : ViewModel(), ScreenViewModel {

    init {
        viewModelScope.launch {
            liveAudioService.audioSourceState.collect { state ->
                if (state == AudioSourceState.READY) {
                    // Start recording audio whenever audio source is done initializing
                    startRecordingAudio()
                }
            }
        }
    }

    /**
     * Displays a Play/Pause button to pause or resume listening to incoming audio.
     *
     * TODO: This should be refined to provide start / stop / pause / resume controls
     */
    override val actions: Flow<List<AppBarButtonViewModel>> =
        liveAudioService.isRunning.map { isRunning ->
            listOf(
                AppBarButtonViewModel(
                    icon = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    onClick = {
                        if (isRunning) {
                            stopRecordingAudio()
                        } else {
                            startRecordingAudio()
                        }
                    }
                )
            )
        }

    fun setupAudioSource() = liveAudioService.setupAudioSource()

    fun startRecordingAudio() = liveAudioService.startListening()

    fun stopRecordingAudio() = liveAudioService.stopListening()

    fun releaseAudioSource() = liveAudioService.releaseAudioSource()
}
