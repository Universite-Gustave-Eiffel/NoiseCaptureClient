package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.history
import noisecapture.composeapp.generated.resources.home_last_measurements_section_header
import noisecapture.composeapp.generated.resources.home_open_history_button_title
import noisecapture.composeapp.generated.resources.home_statistics_recordings_count
import noisecapture.composeapp.generated.resources.home_statistics_title
import noisecapture.composeapp.generated.resources.home_statistics_total
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise


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

    when (viewState) {
        is LastMeasurementsViewModel.ViewState.Loading -> LastMeasurementsViewLoading()

        is LastMeasurementsViewModel.ViewState.ContentReady -> LastMeasurementsViewContentReady(
            viewState as LastMeasurementsViewModel.ViewState.ContentReady,
            onClickMeasurement = onClickMeasurement,
            onClickOpenHistoryButton = onClickOpenHistoryButton,
            modifier = modifier,
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LastMeasurementsViewContentReady(
    viewState: LastMeasurementsViewModel.ViewState.ContentReady,
    onClickMeasurement: (Measurement) -> Unit,
    onClickOpenHistoryButton: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    if (viewState.lastMeasurementIds.isEmpty()) {
        return
    }

    Column(modifier = modifier) {
        ListSectionHeader(
            title = Res.string.home_last_measurements_section_header,
            modifier = Modifier.padding(start = 12.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(IntrinsicSize.Max)
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(IntrinsicSize.Min)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.home_statistics_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                StatisticsElement(
                    viewState.measurementsCount.toString(),
                    stringResource(Res.string.home_statistics_recordings_count)
                )
                StatisticsElement(
                    viewState.totalDuration,
                    "${viewState.durationUnit} ${stringResource(Res.string.home_statistics_total)}"
                )

                Spacer(modifier = Modifier.weight(1f))

                NCButton(
                    onClick = onClickOpenHistoryButton,
                    content = ButtonContent(
                        title = Res.string.home_open_history_button_title,
                        icon = Res.drawable.history,
                    ),
                    colors = Color.Noise.three.tertiaryContainerColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FlowRow(
                maxItemsInEachRow = 2,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                viewState.lastMeasurementIds.forEach { measurementId ->
                    HomeRecentMeasurementView(
                        measurementId = measurementId,
                        onClick = onClickMeasurement,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}


@Composable
private fun LastMeasurementsViewLoading(modifier: Modifier = Modifier) =
    // Loading placeholder layout goes here
    Box(modifier.background(Color.Red))


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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alignByBaseline()
        )
    }
}
