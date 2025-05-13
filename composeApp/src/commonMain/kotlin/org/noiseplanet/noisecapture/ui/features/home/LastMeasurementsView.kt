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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_last_measurements_section_header
import org.noiseplanet.noisecapture.ui.components.CardView
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import kotlin.time.Duration


@Composable
fun LastMeasurementsView(
    viewModel: LastMeasurementsViewModel,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {

    // - Properties

    val lastTwoMeasurements by viewModel.lastTwoMeasurementsFlow.collectAsState(emptyList())
    val measurementsCount by viewModel.measurementsCountFlow.collectAsState(0)
    val totalDuration by viewModel.totalMeasurementsDurationFlow.collectAsState(Duration.ZERO)

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
        CardView(
            backgroundColor = MaterialTheme.colorScheme.surface,
            onClick = viewModel.onClickOpenHistoryButton,
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
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

                NCButton(viewModel = viewModel.openHistoryButtonViewModel)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
        ) {
            lastTwoMeasurements.forEach { measurement ->
                HomeRecentMeasurementView(
                    measurement,
                    onClick = viewModel.onClickMeasurement,
                )
            }
        }
    }
}
