package org.noiseplanet.noisecapture.ui.features.recording.controls

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.RecordingService
import kotlin.time.Duration

class RecordingControlsViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val recordingService: RecordingService by inject()
    private val liveAudioService: LiveAudioService by inject()

    val isRecordingFlow: StateFlow<Boolean> = recordingService.isRecordingFlow
    val isAudioSourceRunningFlow: StateFlow<Boolean> = liveAudioService.isRunningFlow
    val recordingDurationFlow: StateFlow<Duration> = recordingService.recordingDurationFlow


    // - Public functions

    fun togglePauseResume() {
        if (liveAudioService.isRunning) {
            recordingService.pause()
        } else {
            recordingService.resume()
        }
    }

    fun startRecording() {
        recordingService.start()
    }
}
