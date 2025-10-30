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
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RecordingControlsViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val measurementRecordingService: MeasurementRecordingService by inject()
    private val liveAudioService: LiveAudioService by inject()

    val isRecordingFlow: StateFlow<Boolean> = measurementRecordingService.isRecordingFlow
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
        if (measurementRecordingService.isRecording) {
            measurementRecordingService.endAndSave()
            stopTimer()
            _recordingDurationFlow.tryEmit(Duration.ZERO)
        } else {
            measurementRecordingService.start()
            startTimer()
        }
    }

    fun registerListener(onMeasurementDone: (String) -> Unit) {
        measurementRecordingService.onMeasurementDone =
            object : MeasurementRecordingService.OnMeasurementDoneListener {
                override fun onDone(measurementUuid: String) {
                    onMeasurementDone(measurementUuid)
                }
            }
    }

    fun deregisterListener() {
        measurementRecordingService.onMeasurementDone = null
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
