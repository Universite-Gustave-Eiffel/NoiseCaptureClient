package org.noiseplanet.noisecapture.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.MonthNames


object DateUtil {

    object Format {

        val MEASUREMENT_START_DATETIME = LocalDateTime.Format {
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
}
