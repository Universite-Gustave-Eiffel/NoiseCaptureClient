package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.util.DateUtil
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
class MeasurementDetailsChartsViewModel(
    val measurementId: String,
) : ViewModel(), KoinComponent {

    // - ViewState

    sealed interface ViewState {

        data object Loading : ViewState

        data class ContentReady(
            val measurement: Measurement,
            val startTimeString: String,
            val durationString: String,
        ) : ViewState
    }


    // - Properties

    private val measurementService: MeasurementService by inject()

    val viewStateFlow: StateFlow<ViewState> = measurementService.getMeasurementFlow(measurementId)
        .filterNotNull()
        .map { measurement ->
            ViewState.ContentReady(
                measurement,
                startTimeString = getMeasurementStartTimeString(measurement),
                durationString = getMeasurementDurationString(measurement),
            )
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState.Loading,
        )


    // - Private functions

    private fun getMeasurementStartTimeString(measurement: Measurement): String {
        val instant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return localDateTime.format(DateUtil.Format.MEASUREMENT_START_DATETIME)
    }

    private fun getMeasurementDurationString(measurement: Measurement): String {
        val startInstant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val endInstant = Instant.fromEpochMilliseconds(measurement.endTimestamp)
        val duration = endInstant - startInstant

        return duration.toComponents { hours, minutes, seconds, _ ->
            "${hours}h ${minutes}m ${seconds}s"
        }
    }
}
