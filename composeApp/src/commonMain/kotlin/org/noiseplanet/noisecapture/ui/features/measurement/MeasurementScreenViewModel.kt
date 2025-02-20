package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControlsViewModel

class MeasurementScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val recordingService: MeasurementRecordingService by inject()

    val soundLevelMeterViewModel: SoundLevelMeterViewModel by inject()

    val recordingControlsViewModel = RecordingControlsViewModel(
        isPlaying = liveAudioService.isRunningFlow,
        isRecording = recordingService.isRecordingFlow,
        onPlayPauseButtonClick = {
            if (liveAudioService.isRunning) {
                liveAudioService.stopListening()
            } else {
                liveAudioService.startListening()
            }
        },
        onStartStopButtonClick = {
            if (recordingService.isRecording) {
                recordingService.endAndSave()
            } else {
                recordingService.start()
            }
        }
    )
}
