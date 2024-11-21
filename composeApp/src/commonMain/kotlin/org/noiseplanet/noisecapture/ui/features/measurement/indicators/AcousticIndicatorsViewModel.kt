package org.noiseplanet.noisecapture.ui.features.measurement.indicators

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import org.noiseplanet.noisecapture.services.LiveRecordingService

class AcousticIndicatorsViewModel(
    private val measurementService: LiveRecordingService,
) : ViewModel() {

    companion object {

        /**
         * Number of ticks to display along the X-Axis
         * Tick values will be determined from provided min and max values
         */
        const val VU_METER_TICKS_COUNT: Int = 6

        const val VU_METER_DB_MIN = 20.0
        const val VU_METER_DB_MAX = 120.0
    }

    val vuMeterTicks: IntArray = IntArray(size = VU_METER_TICKS_COUNT) { index ->
        (VU_METER_DB_MIN + ((VU_METER_DB_MAX - VU_METER_DB_MIN) / (VU_METER_TICKS_COUNT - 1) * index)).toInt()
    }

    val soundPressureLevelFlow: Flow<Double> = measurementService.getWeightedLeqFlow()
}
