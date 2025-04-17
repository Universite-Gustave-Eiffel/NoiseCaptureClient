package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView

/**
 * Home screen layout.
 */
@OptIn(FormatStringsInDatetimeFormats::class)
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

            // TODO: Add last measurements section

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


@Composable
private fun SoundLevelMeterHeaderView(
    viewModel: HomeScreenViewModel,
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        SoundLevelMeterView(viewModel.soundLevelMeterViewModel)

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            Text(
                text = stringResource(viewModel.hintText),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.75f)
            )

            NCButton(
                viewModel = viewModel.soundLevelMeterButtonViewModel,
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth()
            )
        }
    }
}
