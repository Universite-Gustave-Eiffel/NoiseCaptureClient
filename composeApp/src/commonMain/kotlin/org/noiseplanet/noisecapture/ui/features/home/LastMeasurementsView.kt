package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_last_measurements_section_header
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.CardView
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Composable
fun LastMeasurementsView(
    measurements: List<Measurement>,
    onClickMeasurement: (Measurement) -> Unit,
    onClickShowAllMeasurements: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {

    // - Properties

    val lastTwoMeasurements = measurements
        .sortedByDescending { it.startTimestamp }
        .take(2)

    val measurementsCount = measurements.size
    val totalDuration = if (measurements.isNotEmpty()) {
        measurements.map { it.duration }
            .reduce { total, duration -> total + duration }
            .toDuration(unit = DurationUnit.MILLISECONDS)
    } else {
        Duration.ZERO
    }

    val statisticsValueStyle = MaterialTheme.typography.titleMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )
    val statisticsLabelStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )


    // - Layout

    ListSectionHeader(
        title = Res.string.home_last_measurements_section_header,
        paddingTop = 24.dp,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
        ) {
            lastTwoMeasurements.forEach { measurement ->
                HomeRecentMeasurementView(
                    measurement,
                    onClick = onClickMeasurement,
                )
            }
        }
        CardView(
            backgroundColor = MaterialTheme.colorScheme.surface,
            onClick = onClickShowAllMeasurements,
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = measurementsCount.toString(),
                        style = statisticsValueStyle,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = "recordings",
                        style = statisticsLabelStyle,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = HumanReadable.duration(totalDuration),
                        style = statisticsValueStyle,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = "analysed",
                        style = statisticsLabelStyle,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }
}
