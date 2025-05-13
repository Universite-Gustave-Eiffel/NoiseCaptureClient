package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.sound_level_meter_avg_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_current_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_max_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_min_dba
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.model.dao.LAeqMetrics
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.ButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.ButtonViewModel
import org.noiseplanet.noisecapture.util.VuMeterOptions

class SoundLevelMeterViewModel(
    val showMinMaxSPL: Boolean = true,
    val showPlayPauseButton: Boolean = false,
) : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        /**
         * Number of ticks to display along the X-Axis
         * Tick values will be determined from provided min and max values
         */
        const val VU_METER_TICKS_COUNT: Int = 6
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val measurementService: MeasurementService by inject()

    val playPauseButtonViewModel = ButtonViewModel(
        onClick = this::toggleAudioSource,
        icon = liveAudioService.isRunningFlow.map { isRunning ->
            if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow
        },
        style = flowOf(ButtonStyle.SECONDARY)
    )

    val vuMeterTicks: IntArray = IntArray(size = VU_METER_TICKS_COUNT) { index ->
        val offset = (VuMeterOptions.DB_MAX - VuMeterOptions.DB_MIN) / (VU_METER_TICKS_COUNT - 1)
        (VuMeterOptions.DB_MIN + (offset * index)).toInt()
    }

    val soundPressureLevelFlow: Flow<Double>
        get() = liveAudioService.getWeightedLeqFlow()

    val laeqMetricsFlow: Flow<LAeqMetrics?>
        get() = measurementService.getOngoingMeasurementLaeqMetricsFlow()

    val currentDbALabel = Res.string.sound_level_meter_current_dba
    val minDbALabel = Res.string.sound_level_meter_min_dba
    val avgDbALabel = Res.string.sound_level_meter_avg_dba
    val maxDbALabel = Res.string.sound_level_meter_max_dba


    // - Lifecycle

    init {
        viewModelScope.launch {
            liveAudioService.audioSourceStateFlow.collect { state ->
                if (state == AudioSourceState.READY) {
                    // Start listening to incoming audio whenever audio source is done initializing
                    // On web audio source setup is asynchronous so starting it right away won't work
                    liveAudioService.startListening()
                }
            }
        }
    }


    // - Private functions

    private fun toggleAudioSource() {
        if (liveAudioService.isRunning) {
            liveAudioService.stopListening()
        } else {
            liveAudioService.startListening()
        }
    }
}
