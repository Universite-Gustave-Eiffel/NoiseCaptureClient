package org.noiseplanet.noisecapture.ui.features.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import nl.jacobras.humanreadable.HumanReadable
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.spl.LAeqMetricsView
import org.noiseplanet.noisecapture.util.DateUtil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
@Composable
fun MeasurementHistoryItemView(
    measurement: Measurement,
    onClick: (Measurement) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val instant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val startTime = localDateTime.format(DateUtil.Format.MEASUREMENT_START_DATETIME)


    // - Layout

    Row(
        modifier = modifier.clickable { onClick(measurement) },
    ) {
        Column(
            modifier.padding(16.dp)
        ) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = HumanReadable.duration(measurement.duration.milliseconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                // TODO: Get description from measurement
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
                    "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad" +
                    " minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip" +
                    " ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur " +
                    "sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt " +
                    "mollit anim id est laborum.",
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(8.dp))

            LAeqMetricsView(
                measurement.laeqMetrics,
                modifier = Modifier.height(IntrinsicSize.Max)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
