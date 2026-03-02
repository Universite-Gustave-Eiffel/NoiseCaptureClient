package org.noiseplanet.noisecapture.ui.features.calibration

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_start_button_title
import noisecapture.composeapp.generated.resources.calibration_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.MicrophoneCalibrationProfile
import org.noiseplanet.noisecapture.model.enums.CalibrationFrequencyBand
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.audio.MicrophoneProviderService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.util.dbAverage
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


class CalibrationScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Constants

    companion object {

        private val DEFAULT_CALIBRATION_DURATION: Duration = 10.seconds

        private val COUNTDOWN_DURATION: Duration = 3.seconds
        private val COUNTDOWN_REFRESH_RATE: Duration = 125.milliseconds

        /**
         * If the absolute suggested gain is above this value, show a warning state.
         */
        private const val CALIBRATION_WARNING_THRESHOLD: Double = 20.0
    }


    // - View sate

    sealed interface ViewState {

        data class Configure(
            val duration: Duration,
            val frequencyBand: CalibrationFrequencyBand,
        ) : ViewState

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
            val isWarning: Boolean = false,
        ) : ViewState
    }


    // - Properties

    override val title: StringResource = Res.string.calibration_title

    override val confirmPopBackStack: () -> Boolean
        get() = {
            when (viewState.value) {
                is ViewState.Configure -> true
                else -> {
                    cancelCalibration()
                    false
                }
            }
        }

    private val liveAudioService: LiveAudioService by inject()
    private val microphoneProvider: MicrophoneProviderService by inject()

    private var ongoingJob: Job? = null

    private val _viewState = MutableStateFlow<ViewState>(
        value = ViewState.Configure(
            DEFAULT_CALIBRATION_DURATION,
            CalibrationFrequencyBand.WHOLE_SPECTRUM
        )
    )
    val viewState: StateFlow<ViewState> = _viewState

    private var calibrationDuration = DEFAULT_CALIBRATION_DURATION
    private var calibrationFrequencyBand = CalibrationFrequencyBand.WHOLE_SPECTRUM

    val currentCalibrationProfile = microphoneProvider.currentCalibrationProfile
    val startButtonViewModel = NCButtonViewModel(
        title = Res.string.calibration_start_button_title,
        icon = Icons.Default.Mic,
    )


    // - Public functions

    fun startCalibration(duration: Duration, frequencyBand: CalibrationFrequencyBand) {
        calibrationDuration = duration
        calibrationFrequencyBand = frequencyBand

        _viewState.tryEmit(ViewState.Countdown(COUNTDOWN_DURATION, COUNTDOWN_DURATION))

        // Starts a countdown job that will periodically update UI with remaining time
        ongoingJob = viewModelScope.launch {
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
                    duration = calibrationDuration,
                    timeLeft = calibrationDuration,
                    currentAverage = 0.0,
                )
            )
            startRecording()
        }
    }

    fun cancelCalibration() {
        ongoingJob?.cancel()
        ongoingJob = null
        _viewState.tryEmit(ViewState.Configure(calibrationDuration, calibrationFrequencyBand))
    }

    fun onReferenceValueChange(newReferenceValue: Double?) {
        val state = viewState.value as ViewState.Results
        val suggestedGain = newReferenceValue?.let {
            (it - (state.measuredValue - state.currentGain)).roundTo(1)
        }
        val isWarning = (suggestedGain?.absoluteValue ?: 0.0) >= CALIBRATION_WARNING_THRESHOLD

        _viewState.tryEmit(
            state.copy(
                difference = newReferenceValue?.let {
                    (it - state.measuredValue).roundTo(1)
                },
                suggestedGain = suggestedGain,
                isWarning = isWarning,
            )
        )
    }

    fun saveGain(gain: Double, completionHandler: () -> Unit) {
        val preferredInput = microphoneProvider.preferredInput.value ?: return

        viewModelScope.launch {
            microphoneProvider.saveCalibrationProfile(
                MicrophoneCalibrationProfile(
                    calibrationTimestamp = Clock.System.now().toEpochMilliseconds(),
                    isCalibrated = true,
                    compensationGain = gain,
                    microphoneInfo = preferredInput,
                )
            )
            completionHandler()
        }
    }


    // - Private functions

    private fun startRecording() {
        // Start audio source and collect all incoming levels
        liveAudioService.startListening()
        ongoingJob = viewModelScope.launch(Dispatchers.Default) {
            // Keep track of all recorded levels to recalculate average on every new value
            val measuredValues: MutableList<Double> = mutableListOf()
            val startTimestamp: Long = Clock.System.now().toEpochMilliseconds()

            liveAudioService.getLeqRecordsFlow().collect { leqRecord ->
                when (calibrationFrequencyBand) {
                    // If using whole spectrum, use LAEq as value
                    CalibrationFrequencyBand.WHOLE_SPECTRUM -> {
                        measuredValues.add(leqRecord.laeq)
                    }

                    // Otherwise, look for the value of the corresponding frequency band
                    else -> {
                        leqRecord.leqsPerThirdOctave[calibrationFrequencyBand.centerFrequency]?.let {
                            measuredValues.add(it)
                        }
                    }
                }

                // Update UI
                val timeLeft = calibrationDuration -
                    (leqRecord.timestamp - startTimestamp).milliseconds
                val state = ViewState.Recording(
                    duration = calibrationDuration,
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
