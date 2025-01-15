package org.noiseplanet.noisecapture.ui.components.spl

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.sound_level_meter_avg_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_current_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_max_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_min_dba
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService

class SoundLevelMeterViewModel : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        /**
         * Number of ticks to display along the X-Axis
         * Tick values will be determined from provided min and max values
         */
        const val VU_METER_TICKS_COUNT: Int = 6

        const val VU_METER_DB_MIN = 20.0
        const val VU_METER_DB_MAX = 120.0
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()

    var showMinMaxSPL: Boolean = true
    var showPlayPauseButton: Boolean = false

    val vuMeterTicks: IntArray = IntArray(size = VU_METER_TICKS_COUNT) { index ->
        (VU_METER_DB_MIN + ((VU_METER_DB_MAX - VU_METER_DB_MIN) / (VU_METER_TICKS_COUNT - 1) * index)).toInt()
    }

    val soundPressureLevelFlow: Flow<Double>
        get() = liveAudioService.getWeightedLeqFlow()

    val isAudioSourceRunningFlow: Flow<Boolean>
        get() = liveAudioService.isRunningFlow

    val currentDbALabel = Res.string.sound_level_meter_current_dba
    val minDbALabel = Res.string.sound_level_meter_min_dba
    val avgDbALabel = Res.string.sound_level_meter_avg_dba
    val maxDbALabel = Res.string.sound_level_meter_max_dba


    // - Public functions

    fun startListening() {
        liveAudioService.startListening()
    }

    fun stopListening() {
        liveAudioService.stopListening()
    }
}
