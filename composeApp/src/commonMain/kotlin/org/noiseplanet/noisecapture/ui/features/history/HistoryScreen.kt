package org.noiseplanet.noisecapture.ui.features.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(FormatStringsInDatetimeFormats::class, KoinExperimentalAPI::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryScreenViewModel,
) {

    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(historyModule)
    }


    // - Properties

    val measurements by viewModel.measurementsFlow.collectAsState(emptyList())


    // - Layout

    // Temporary layout to visualise the measurements currently stored
    LazyColumn {
        items(measurements) { measurement ->
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Measurement: ${measurement.uuid}")

                val dateFormat = DateTimeComponents.Format {
                    byUnicodePattern("dd/MM/yyyy HH:mm:ss")
                }
                val timeFormat = DateTimeComponents.Format {
                    byUnicodePattern("HH:mm:ss")
                }
                val startDate = Instant.fromEpochMilliseconds(measurement.startTimestamp)
                val endDate = Instant.fromEpochMilliseconds(measurement.endTimestamp)
                val duration = Instant.fromEpochMilliseconds(measurement.duration)

                Text("Started at (UTC): ${startDate.format(dateFormat)}")
                Text("Ended at (UTC): ${endDate.format(dateFormat)}")
                Text("Duration: ${duration.format(timeFormat)}")
            }
        }
    }
}
