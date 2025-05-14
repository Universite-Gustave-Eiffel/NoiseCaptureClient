package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.sound_level_meter_avg_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_max_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_min_dba
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.LAeqMetrics
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.isInVuMeterRange


@Composable
fun LAeqMetricsView(
    metrics: LAeqMetrics?,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val minDbALabel = Res.string.sound_level_meter_min_dba
    val avgDbALabel = Res.string.sound_level_meter_avg_dba
    val maxDbALabel = Res.string.sound_level_meter_max_dba


    // - Layout

    Row(modifier = modifier) {
        listOf(
            LeqMetricViewModel(
                label = stringResource(minDbALabel),
                value = metrics?.min,
            ),
            LeqMetricViewModel(
                label = stringResource(avgDbALabel),
                value = metrics?.average,
            ),
            LeqMetricViewModel(
                label = stringResource(maxDbALabel),
                value = metrics?.max,
            ),
        ).forEach { metric ->
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(56.dp),
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelLarge
                )

                val value = metric.value
                BasicText(
                    text = if (value != null && value.isInVuMeterRange()) {
                        value.toString()
                    } else "-",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp,
                        color = metric.value?.let {
                            NoiseLevelColorRamp.getColorForSPLValue(it)
                        } ?: MaterialTheme.colorScheme.onSurface,
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 20.sp,
                        stepSize = 1.sp,
                    )
                )
            }
        }
    }
}


private data class LeqMetricViewModel(
    val label: String,
    val value: Double?,
)
