package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import kotlinx.datetime.Instant
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


class MeasurementDetailsChartsViewModel(
    val measurement: Measurement,
) : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        private val DATE_TIME_FORMAT = LocalDateTime.Format {
            date(LocalDate.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(". ")
                dayOfMonth()
                chars(" ")
                year()
            })
            chars(", at ")
            time(LocalTime.Format {
                hour(); chars(":"); minute()
            })
        }
    }


    // - Properties

    private val measurementService: MeasurementService by inject()


    // - Public functions

    fun getMeasurementStartTimeString(): String {
        val instant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return localDateTime.format(DATE_TIME_FORMAT)
    }

    fun getMeasurementDurationString(): String {
        val startInstant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val endInstant = Instant.fromEpochMilliseconds(measurement.endTimestamp)
        val duration = endInstant - startInstant

        return duration.toComponents { hours, minutes, seconds, _ ->
            "${hours}h ${minutes}m ${seconds}s"
        }
    }
}
