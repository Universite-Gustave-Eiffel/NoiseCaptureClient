package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_average_level
import noisecapture.composeapp.generated.resources.measurement_no_description_placeholder
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.roundTo


@Composable
fun DetailsChartsHeader(
    startTime: String,
    duration: String,
    averageLevel: Double,
) {
    // - Properties

    val averageLevelColorTint: Color = NoiseLevelColorRamp.getColorForSPLValue(
        value = averageLevel,
        palette = NoiseLevelColorRamp.paletteDarker,
    )
    val averageLevelColorBackground: Color = NoiseLevelColorRamp.getColorForSPLValue(
        value = averageLevel,
        palette = NoiseLevelColorRamp.paletteLighter,
    )


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clip(RoundedCornerShape(size = 16.dp))
                .background(averageLevelColorBackground)
                .padding(20.dp)
        ) {
            Text(
                text = averageLevel.roundTo(1).toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = averageLevelColorTint,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                lineHeight = 40.sp,
            )
            Text(
                text = stringResource(Res.string.measurement_details_average_level),
                style = MaterialTheme.typography.labelSmall,
                color = averageLevelColorTint,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                // TODO: Get description from measurement
                text = stringResource(Res.string.measurement_no_description_placeholder),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
    }
}
