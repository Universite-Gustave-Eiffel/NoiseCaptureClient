package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.byUnicodePattern
import org.noiseplanet.noisecapture.model.dao.Measurement

/**
 * Home screen layout.
 */
//@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
) {
    // - Properties

    val measurements = remember { mutableStateListOf<Measurement>() }

    LaunchedEffect(viewModel.viewModelScope) {
        measurements.clear()
        measurements.addAll(viewModel.getStoredMeasurements())
    }


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            SoundLevelMeterHeaderView(viewModel)

            LastMeasurementsView(
                measurements,
                onClickMeasurement = {
                    // TODO: Open measurement details screen
                },
                onClickShowAllMeasurements = {
                    // TODO: Open measurements history screen
                }
            )

            // For debug purposes to visualise the measurements currently stored
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

            // TODO: Add device calibration section

            // TODO: Add more info section
        }
    }
}
