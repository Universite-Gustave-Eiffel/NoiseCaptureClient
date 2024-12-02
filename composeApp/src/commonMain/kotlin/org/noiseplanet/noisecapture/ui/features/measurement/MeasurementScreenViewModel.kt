package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControlsViewModel

class MeasurementScreenViewModel(
    private val liveAudioService: LiveAudioService,
    private val recordingService: MeasurementRecordingService,
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

    val recordingControlsViewModel = RecordingControlsViewModel(
        isPlaying = liveAudioService.isRunningFlow,
        isRecording = recordingService.isRecordingFlow,
        onPlayPauseButtonClick = {
            if (liveAudioService.isRunning) {
                stopRecordingAudio()
            } else {
                startRecordingAudio()
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

    fun setupAudioSource() = liveAudioService.setupAudioSource()

    fun startRecordingAudio() = liveAudioService.startListening()

    fun stopRecordingAudio() = liveAudioService.stopListening()

    fun releaseAudioSource() = liveAudioService.releaseAudioSource()
}
