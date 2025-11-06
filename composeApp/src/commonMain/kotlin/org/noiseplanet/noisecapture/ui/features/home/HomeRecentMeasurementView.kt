package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_last_measurement_duration
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.spl.LAeqMetricsView
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toDuration


@OptIn(ExperimentalTime::class)
@Composable
fun HomeRecentMeasurementView(
    measurement: Measurement,
    onClick: (Measurement) -> Unit,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val durationPrefix = stringResource(Res.string.home_last_measurement_duration)
    val startTime = Instant.fromEpochMilliseconds(measurement.startTimestamp)
    val duration = measurement.duration.toDuration(unit = DurationUnit.MILLISECONDS)


    // - Layout

    Column(
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
            .clickable { onClick(measurement) }
            .padding(top = 12.dp, bottom = 12.dp, start = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = HumanReadable.timeAgo(startTime),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = "${durationPrefix}: ${HumanReadable.duration(duration)}",
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        )

        LAeqMetricsView(
            measurement.laeqMetrics,
            modifier = Modifier.padding(top = 8.dp)
                .height(IntrinsicSize.Max)
        )
    }
}
