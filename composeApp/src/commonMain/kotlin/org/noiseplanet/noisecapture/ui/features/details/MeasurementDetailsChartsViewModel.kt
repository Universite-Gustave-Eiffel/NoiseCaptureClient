package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.ui.graphics.Color
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
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.roundTo


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
        val startInstant = Instant.fromEpochMilliseconds(measurement.endTimestamp)
        val endInstant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val duration = endInstant - startInstant

        return duration.toComponents { hours, minutes, seconds, _ ->
            "${hours}h ${minutes}m ${seconds}s"
        }
    }

    fun getMeasurementAverageLevel(): String {
        return measurement.laeqMetrics.average.roundTo(1).toString()
    }

    fun getMeasurementAverageLevelColor(): Color {
        return NoiseLevelColorRamp.getColorForSPLValue(measurement.laeqMetrics.average)
    }
}
