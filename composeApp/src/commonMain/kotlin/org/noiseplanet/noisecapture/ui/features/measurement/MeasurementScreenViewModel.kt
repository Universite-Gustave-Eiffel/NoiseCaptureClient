package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.noiseplanet.noisecapture.services.LiveAudioService
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class MeasurementScreenViewModel(
    private val liveAudioService: LiveAudioService,
) : ViewModel(), ScreenViewModel {

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
