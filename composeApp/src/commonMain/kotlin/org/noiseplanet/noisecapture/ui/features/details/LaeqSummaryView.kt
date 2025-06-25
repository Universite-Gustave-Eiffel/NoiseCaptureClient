package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_laeq_summary_la10
import noisecapture.composeapp.generated.resources.measurement_details_laeq_summary_la50
import noisecapture.composeapp.generated.resources.measurement_details_laeq_summary_la90
import noisecapture.composeapp.generated.resources.measurement_details_laeq_summary_max
import noisecapture.composeapp.generated.resources.measurement_details_laeq_summary_min
import noisecapture.composeapp.generated.resources.measurement_details_laeq_summary_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.isInVuMeterRange
import org.noiseplanet.noisecapture.util.roundTo


@Composable
fun LaeqSummaryView(
    min: Double,
    la10: Double,
    la50: Double,
    la90: Double,
    max: Double,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(Res.string.measurement_details_laeq_summary_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = buildAnnotatedString {
                val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
                withStyle(style = boldStyle) {
                    append(" • Min: ")
                }
                append(stringResource(Res.string.measurement_details_laeq_summary_min) + "\n")
                withStyle(style = boldStyle) {
                    append(" • LA90: ")
                }
                append(stringResource(Res.string.measurement_details_laeq_summary_la90) + "\n")
                withStyle(style = boldStyle) {
                    append(" • LA50: ")
                }
                append(stringResource(Res.string.measurement_details_laeq_summary_la50) + "\n")
                withStyle(style = boldStyle) {
                    append(" • LA10: ")
                }
                append(stringResource(Res.string.measurement_details_laeq_summary_la10) + "\n")
                withStyle(style = boldStyle) {
                    append(" • Max: ")
                }
                append(stringResource(Res.string.measurement_details_laeq_summary_max) + "\n")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            LaeqSummaryItemView(label = "Min", value = min, isFirst = true)
            LaeqSummaryItemView(label = "LA90", value = la90)
            LaeqSummaryItemView(label = "LA50", value = la50)
            LaeqSummaryItemView(label = "LA10", value = la10)
            LaeqSummaryItemView(label = "Max", value = max, isLast = true)
        }
    }
}


@Composable
private fun RowScope.LaeqSummaryItemView(
    label: String,
    value: Double,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    // - Properties

    val barHeight = 8.dp
    val barColor = if (value.isInVuMeterRange()) {
        NoiseLevelColorRamp.getColorForSPLValue(value)
    } else {
        Color.LightGray
    }
    val barShape = RoundedCornerShape(
        topStart = if (isFirst) barHeight / 2 else 0.dp,
        topEnd = if (isLast) barHeight / 2 else 0.dp,
        bottomStart = if (isFirst) barHeight / 2 else 0.dp,
        bottomEnd = if (isLast) barHeight / 2 else 0.dp,
    )


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(barShape)
                .background(barColor)
                .height(barHeight)
        )

        Text(
            text = if (value.isInVuMeterRange()) value.roundTo(1).toString() else "-",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
