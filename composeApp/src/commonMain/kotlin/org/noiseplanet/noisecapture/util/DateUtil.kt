package org.noiseplanet.noisecapture.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant


object DateUtil {

    object Format {

        val MEASUREMENT_START_DATETIME = LocalDateTime.Format {
            date(LocalDate.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED); chars(". "); day(); chars(" "); year()
            })
            chars(", at ")
            time(LocalTime.Format {
                hour(); chars(":"); minute()
            })
        }

        val CALIBRATION_DATETIME = LocalDateTime.Format {
            date(LocalDate.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED); chars(". "); day(); chars(" "); year()
            })
        }

        val SHORT_DATETIME_FORMAT = LocalDateTime.Format {
            date(LocalDate.Format {
                day(); char('/'); monthNumber(); char('/'); year()
            })
            chars(" at ")
            time(LocalTime.Format {
                hour(); char(':'); minute()
            })
        }
    }
}

fun Long.millisecondsToFormattedDateTime(format: DateTimeFormat<LocalDateTime>): String {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .format(format)
}
