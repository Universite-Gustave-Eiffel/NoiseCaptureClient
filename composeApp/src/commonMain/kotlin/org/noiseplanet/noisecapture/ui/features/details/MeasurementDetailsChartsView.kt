package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerView


@Composable
fun MeasurementDetailsChartsView(
    measurementId: String,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: MeasurementDetailsChartsViewModel = koinViewModel {
        parametersOf(measurementId)
    }
    val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()


    // - Layout

    when (viewState) {
        is MeasurementDetailsChartsViewModel.ViewState.ContentReady -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = modifier.fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            ) {
                val state = viewState as MeasurementDetailsChartsViewModel.ViewState.ContentReady

                MeasurementDetailsChartsHeader(
                    startTime = state.startTimeString,
                    duration = state.durationString,
                    averageLevel = state.measurement.laeqMetrics.average,
                )

                state.measurement.recordedAudioUrl?.let { audioUrl ->
                    AudioPlayerView(audioUrl)
                }

                MeasurementSplTimePlotView(measurementId)

                LaeqSummaryView(
                    min = state.measurement.laeqMetrics.min,
                    la90 = state.measurement.summary?.la90 ?: 0.0,
                    la50 = state.measurement.summary?.la50 ?: 0.0,
                    la10 = state.measurement.summary?.la10 ?: 0.0,
                    max = state.measurement.laeqMetrics.max
                )

                ManageMeasurementView(state.measurement.uuid)
            }
        }

        else -> return
    }
}
