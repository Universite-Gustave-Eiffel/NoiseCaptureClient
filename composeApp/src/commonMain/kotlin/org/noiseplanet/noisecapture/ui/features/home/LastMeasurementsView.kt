package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_last_measurements_section_header
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.CardView
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.button.NCButton


@Composable
fun LastMeasurementsView(
    onClickMeasurement: (Measurement) -> Unit,
    onClickOpenHistoryButton: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {

    // - Properties

    val viewModel: LastMeasurementsViewModel = koinInject()
    val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()


    // - Layout

    Crossfade(viewState) { viewState ->
        when (viewState) {
            is LastMeasurementsViewModel.ViewState.Loading -> LastMeasurementsViewLoading()

            is LastMeasurementsViewModel.ViewState.ContentReady -> LastMeasurementsViewContentReady(
                viewState,
                onClickMeasurement = onClickMeasurement,
                onClickOpenHistoryButton = onClickOpenHistoryButton,
                modifier,
            )
        }
    }
}


@Composable
private fun LastMeasurementsViewContentReady(
    viewState: LastMeasurementsViewModel.ViewState.ContentReady,
    onClickMeasurement: (Measurement) -> Unit,
    onClickOpenHistoryButton: () -> Unit,
    modifier: Modifier,
) {

    // - Layout

    if (viewState.lastTwoMeasurements.isEmpty()) {
        return
    }

    Column {
        ListSectionHeader(
            title = Res.string.home_last_measurements_section_header,
            paddingTop = 24.dp,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier.padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            CardView(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Min)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                    )

                    StatisticsElement(viewState.measurementsCount.toString(), "recordings")
                    StatisticsElement(viewState.totalDuration, "${viewState.durationUnit} total")

                    Spacer(modifier = Modifier.weight(1f))

                    NCButton(
                        onClick = onClickOpenHistoryButton,
                        viewModel = viewState.historyButtonViewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                viewState.lastTwoMeasurements.forEach { measurement ->
                    HomeRecentMeasurementView(
                        measurement,
                        onClick = onClickMeasurement,
                    )
                }
            }
        }
    }
}


@Composable
private fun LastMeasurementsViewLoading() {
    // Loading placeholder layout goes here
}


@Composable
private fun StatisticsElement(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val statisticsValueStyle = MaterialTheme.typography.titleMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )
    val statisticsLabelStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )


    // - Layout

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = value,
            style = statisticsValueStyle,
            modifier = Modifier.alignByBaseline()
        )
        Text(
            text = label,
            style = statisticsLabelStyle,
            modifier = Modifier.alignByBaseline()
        )
    }
}
