package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_last_measurement_duration
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.CardView
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Composable
fun MeasurementHistoryCardView(
    measurement: Measurement,
    onClick: (Measurement) -> Unit,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val durationPrefix = stringResource(Res.string.home_last_measurement_duration)
    val startTime = Instant.fromEpochMilliseconds(measurement.startTimestamp)
    val duration = measurement.duration.toDuration(unit = DurationUnit.MILLISECONDS)


    // - Layout

    CardView(
        onClick = { onClick(measurement) },
        modifier = modifier,
    ) {
        Column {
            Text(
                text = HumanReadable.timeAgo(startTime),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
            )
            Text(
                text = "${durationPrefix}: ${HumanReadable.duration(duration)}",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )
        }
    }
}
