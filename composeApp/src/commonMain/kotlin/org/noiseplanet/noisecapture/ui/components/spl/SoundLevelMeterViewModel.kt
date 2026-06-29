package org.noiseplanet.noisecapture.ui.components.spl

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
import org.noiseplanet.noisecapture.util.roundTo
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class SoundLevelMeterViewModel(
    val showMinMaxSPL: Boolean,
    val showPlayPauseButton: Boolean,
) : ViewModel(), KoinComponent {

    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val measurementService: MeasurementService by inject()
    private val permissionService: PermissionService by inject()

    val isRunning: StateFlow<Boolean> = liveAudioService.isRunning

    val soundPressureLevelFlow: StateFlow<Double> = liveAudioService
        .getWeightedLeqFlow()
        .map { it.roundTo(1) }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = 0.0,
        )

    val laeqMetricsFlow: StateFlow<LAeqMetrics?> = measurementService
        .getOngoingMeasurementLaeqMetricsFlow()
        .map { it?.copy(average = it.average.roundTo(1)) }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = null,
        )

    val currentDbALabel = Res.string.sound_level_meter_current_dba


    // - Public functions

    fun toggleAudioSource(showPermissionPrompt: (Permission) -> Unit) {
        if (liveAudioService.isRunning.value) {
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
}
