package org.noiseplanet.noisecapture.ui.features.recording.controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.RecordingService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RecordingControlsViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val recordingService: RecordingService by inject()
    private val liveAudioService: LiveAudioService by inject()

    val isRecordingFlow: StateFlow<Boolean> = recordingService.isRecordingFlow
    val isAudioSourceRunningFlow: StateFlow<Boolean> = liveAudioService.isRunningFlow

    private val _recordingDurationFlow = MutableStateFlow(Duration.ZERO)
    val recordingDurationFlow: StateFlow<Duration>
        get() = _recordingDurationFlow.asStateFlow()

    private var timerJob: Job? = null


    // - Public functions

    fun toggleAudioSource() {
        if (liveAudioService.isRunning) {
            stopTimer()
            liveAudioService.stopListening()
        } else {
            startTimer()
            liveAudioService.startListening()
        }
    }

    fun toggleRecording() {
        if (recordingService.isRecording) {
            recordingService.endAndSave()
            stopTimer()
            _recordingDurationFlow.tryEmit(Duration.ZERO)
        } else {
            recordingService.start()
            startTimer()
        }
    }

    fun registerListener(onMeasurementDone: (String) -> Unit) {
        recordingService.onMeasurementDone =
            object : RecordingService.OnMeasurementDoneListener {
                override fun onDone(measurementUuid: String) {
                    onMeasurementDone(measurementUuid)
                }
            }
    }

    fun deregisterListener() {
        recordingService.onMeasurementDone = null
    }


    // - Private functions

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(250.milliseconds)
                _recordingDurationFlow.tryEmit(_recordingDurationFlow.value + 250.milliseconds)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }
}
