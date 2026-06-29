package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.details_average_level
import noisecapture.composeapp.generated.resources.measurement_no_description_placeholder
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.Container
import org.noiseplanet.noisecapture.util.roundTo


@Composable
fun DetailsChartsHeader(
    startTime: String,
    duration: String,
    averageLevel: Double,
) {
    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Container(
            contentPadding = PaddingValues(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = averageLevel.roundTo(1).toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                )
                Text(
                    text = stringResource(Res.string.details_average_level),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                // TODO: Get description from measurement
                text = stringResource(Res.string.measurement_no_description_placeholder),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
