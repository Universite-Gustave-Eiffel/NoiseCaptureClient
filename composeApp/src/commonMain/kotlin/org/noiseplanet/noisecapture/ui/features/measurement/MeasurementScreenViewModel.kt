package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
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
                stopAudioSource()
            } else {
                startAudioSource()
            }
        },
        onStartStopButtonClick = {
            if (recordingService.isRecording) {
                endRecording()
            } else {
                startRecording()
            }
        }
    )

    /**
     * True if a measurement recording is currently ongoing, meaning audio and location services
     * should still be running while the app is sent to background.
     */
    val isRecording: Boolean
        get() = recordingService.isRecording


    // - Lifecycle

    init {
        viewModelScope.launch {
            liveAudioService.audioSourceStateFlow.collect { state ->
                if (state == AudioSourceState.READY) {
                    // Start listening to incoming audio whenever audio source is done initializing
                    soundLevelMeterViewModel.startListening()
                }
            }
        }
    }


    // - Public functions

    fun setupAudioSource() = liveAudioService.setupAudioSource()

    fun startAudioSource() = liveAudioService.startListening()

    fun stopAudioSource() = liveAudioService.stopListening()

    fun releaseAudioSource() = liveAudioService.releaseAudioSource()

    fun startRecording() = recordingService.start()

    fun endRecording() = recordingService.endAndSave()
}
