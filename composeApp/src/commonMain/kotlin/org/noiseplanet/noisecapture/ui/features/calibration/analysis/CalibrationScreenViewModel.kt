package org.noiseplanet.noisecapture.ui.features.calibration.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.MicrophoneCalibrationProfile
import org.noiseplanet.noisecapture.model.enums.CalibrationFrequencyBand
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.audio.MicrophoneProviderService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.util.dbAverage
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


class CalibrationScreenViewModel(
    val duration: Duration,
    val frequencyBand: CalibrationFrequencyBand,
) : ViewModel(), ScreenViewModel, KoinComponent {

    // - Constants

    companion object {

        private val COUNTDOWN_DURATION: Duration = 3.seconds
        private val COUNTDOWN_REFRESH_RATE: Duration = 125.milliseconds
    }


    // - View sate

    sealed interface ViewState {

        data class Countdown(
            val duration: Duration,
            val timeLeft: Duration,
        ) : ViewState

        data class Recording(
            val duration: Duration,
            val timeLeft: Duration,
            val currentAverage: Double,
        ) : ViewState

        data class Results(
            val measuredValue: Double,
            val currentGain: Double,
            val difference: Double? = null,
            val suggestedGain: Double? = null,
        ) : ViewState
    }


    // - Properties

    override val title: StringResource = Res.string.calibration_title

    private val liveAudioService: LiveAudioService by inject()
    private val microphoneProvider: MicrophoneProviderService by inject()

    private val _viewState = MutableStateFlow<ViewState>(
        value = ViewState.Countdown(COUNTDOWN_DURATION, COUNTDOWN_DURATION)
    )
    val viewState: StateFlow<ViewState> = _viewState


    // - Lifecycle

    init {
        startCountdown()
    }


    // - Public functions

    fun onReferenceValueChange(newReferenceValue: Double?) {
        val state = viewState.value as ViewState.Results
        _viewState.tryEmit(
            state.copy(
                difference = newReferenceValue?.let {
                    (it - state.measuredValue).roundTo(1)
                },
                suggestedGain = newReferenceValue?.let {
                    (it - (state.measuredValue - state.currentGain)).roundTo(1)
                }
            )
        )
    }

    val logger by injectLogger()
    fun saveGain(gain: Double, completionHandler: () -> Unit) {
        val preferredInput = microphoneProvider.preferredInput.value ?: return

        viewModelScope.launch {
            microphoneProvider.saveCalibrationProfile(
                MicrophoneCalibrationProfile(
                    calibrationTimestamp = Clock.System.now().toEpochMilliseconds(),
                    compensationGain = gain,
                    microphoneInfo = preferredInput,
                )
            )
            completionHandler()
        }
    }


    // - Private functions

    private fun startCountdown() {
        // Starts a countdown job that will periodically update UI with remaining time
        viewModelScope.launch {
            var timeLeft: Duration = COUNTDOWN_DURATION

            while (timeLeft > Duration.ZERO) {
                delay(COUNTDOWN_REFRESH_RATE)
                timeLeft -= COUNTDOWN_REFRESH_RATE
                _viewState.emit(
                    ViewState.Countdown(duration = COUNTDOWN_DURATION, timeLeft = timeLeft)
                )
            }
            // When countdown is over, start recording
            _viewState.emit(
                ViewState.Recording(
                    duration = duration,
                    timeLeft = duration,
                    currentAverage = 0.0,
                )
            )
            startRecording()
        }
    }

    private fun startRecording() {
        // Start audio source and collect all incoming levels
        liveAudioService.startListening()
        viewModelScope.launch(Dispatchers.Default) {
            // Keep track of all recorded levels to recalculate average on every new value
            val measuredValues: MutableList<Double> = mutableListOf()
            val startTimestamp: Long = Clock.System.now().toEpochMilliseconds()

            liveAudioService.getLeqRecordsFlow().collect { leqRecord ->
                when (frequencyBand) {
                    // If using whole spectrum, use LAEq as value
                    CalibrationFrequencyBand.WHOLE_SPECTRUM -> {
                        measuredValues.add(leqRecord.laeq)
                    }

                    // Otherwise, look for the value of the corresponding frequency band
                    else -> {
                        leqRecord.leqsPerThirdOctave[frequencyBand.centerFrequency]?.let {
                            measuredValues.add(it)
                        }
                    }
                }

                // Update UI
                val timeLeft = duration - (leqRecord.timestamp - startTimestamp).milliseconds
                val state = ViewState.Recording(
                    duration = duration,
                    timeLeft = timeLeft,
                    currentAverage = measuredValues.dbAverage()
                )
                _viewState.emit(state)

                if (timeLeft <= Duration.ZERO) {
                    _viewState.emit(
                        ViewState.Results(
                            measuredValue = measuredValues.dbAverage(),
                            currentGain = getCurrentCompensationGain(),
                        )
                    )
                    liveAudioService.stopListening()
                    cancel()
                }
            }
        }
    }

    private fun getCurrentCompensationGain(): Double {
        return microphoneProvider.currentCalibrationProfile.value
            ?.compensationGain
            ?: 0.0
    }
}
