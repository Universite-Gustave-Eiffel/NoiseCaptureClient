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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.sound_level_meter_avg_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_max_dba
import noisecapture.composeapp.generated.resources.sound_level_meter_min_dba
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.LAeqMetrics
import org.noiseplanet.noisecapture.ui.theme.ColorVariant
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.ui.theme.titleMono
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
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                val value = metric.value
                BasicText(
                    text = if (value != null && value.isInVuMeterRange()) {
                        value.toString()
                    } else "-",
                    style = MaterialTheme.typography.titleMono.copy(
                        color = metric.value?.let {
                            NoiseLevelColorRamp.getColorForSPLValue(
                                value = it,
                                variant = ColorVariant.DARK,
                            )
                        } ?: MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 18.sp,
                        stepSize = 0.25.sp,
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
