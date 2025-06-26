package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
class MeasurementDetailsChartsViewModel(
    val measurementId: String,
) : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        private val DATE_TIME_FORMAT = LocalDateTime.Format {
            date(LocalDate.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(". ")
                day()
                chars(" ")
                year()
            })
            chars(", at ")
            time(LocalTime.Format {
                hour(); chars(":"); minute()
            })
        }
    }


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
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState.Loading,
        )


    // - Private functions

    private fun getMeasurementStartTimeString(measurement: Measurement): String {
        val instant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return localDateTime.format(DATE_TIME_FORMAT)
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
