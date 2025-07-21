package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.sound_level_meter_current_dba
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.LAeqMetrics
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.ui.components.button.IconNCButtonViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.util.VuMeterOptions
import org.noiseplanet.noisecapture.util.roundTo
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class SoundLevelMeterViewModel(
    val showMinMaxSPL: Boolean,
    val showPlayPauseButton: Boolean,
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
    private val permissionService: PermissionService by inject()

    val playPauseButtonViewModelFlow: StateFlow<NCButtonViewModel> = liveAudioService.isRunningFlow
        .map { isRunning ->
            getPlayPauseButtonViewModel(isRunning)
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = getPlayPauseButtonViewModel(liveAudioService.isRunning),
        )

    val vuMeterTicks: IntArray = IntArray(size = VU_METER_TICKS_COUNT) { index ->
        val offset = (VuMeterOptions.DB_MAX - VuMeterOptions.DB_MIN) / (VU_METER_TICKS_COUNT - 1)
        (VuMeterOptions.DB_MIN + (offset * index)).toInt()
    }

    val soundPressureLevelFlow: StateFlow<Double> = liveAudioService
        .getWeightedLeqFlow()
        .map { it.roundTo(1) }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = 0.0,
        )

    val laeqMetricsFlow: StateFlow<LAeqMetrics?> = measurementService
        .getOngoingMeasurementLaeqMetricsFlow()
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = null,
        )

    val currentDbALabel = Res.string.sound_level_meter_current_dba


    // - Public functions

    fun toggleAudioSource(showPermissionPrompt: (Permission) -> Unit) {
        if (liveAudioService.isRunning) {
            liveAudioService.stopListening()
        } else {
            val audioPermissionState = permissionService.getPermissionState(Permission.RECORD_AUDIO)
            if (audioPermissionState == PermissionState.GRANTED) {
                liveAudioService.startListening()
            } else {
                // If record audio permission was not already granted, prompt it to the user.
                showPermissionPrompt(Permission.RECORD_AUDIO)
            }
        }
    }


    // - Private functions

    private fun getPlayPauseButtonViewModel(isAudioSourceRunning: Boolean): NCButtonViewModel {
        val icon = if (isAudioSourceRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow

        return IconNCButtonViewModel(
            icon = icon,
            colors = { NCButtonColors.Defaults.secondary() },
        )
    }
}
