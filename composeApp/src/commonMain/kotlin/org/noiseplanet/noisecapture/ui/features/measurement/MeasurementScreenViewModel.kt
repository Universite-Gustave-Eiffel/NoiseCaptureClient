package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControlsViewModel

class MeasurementScreenViewModel(
    private val liveAudioService: LiveAudioService,
    private val recordingService: MeasurementRecordingService,
) : ViewModel(), ScreenViewModel {

    // - Properties

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
                recordingService.endAndSave()
            } else {
                recordingService.start()
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
                    // Start recording audio whenever audio source is done initializing
                    startAudioSource()
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
