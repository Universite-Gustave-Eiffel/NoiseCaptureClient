package org.noiseplanet.noisecapture.ui.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    isFirstInSection: Boolean,
    isLastInSection: Boolean,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val shape = MaterialTheme.shapes.medium
        .let {
            if (isFirstInSection) it else {
                it.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
            }
        }.let {
            if (isLastInSection) it else {
                it.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))
            }
        }

    val instant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val startTime = localDateTime.format(DateUtil.Format.MEASUREMENT_START_DATETIME)


    // - Layout

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.background(MaterialTheme.colorScheme.surface, shape)
            .clip(shape)
            .clickable { onClick(measurement) }
            .padding(start = 16.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
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
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod",
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 8.dp).size(20.dp),
        )
    }
}
