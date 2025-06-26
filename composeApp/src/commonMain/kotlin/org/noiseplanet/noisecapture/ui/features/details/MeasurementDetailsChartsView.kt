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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerView


@Composable
fun MeasurementDetailsChartsView(
    measurement: Measurement,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: MeasurementDetailsChartsViewModel = koinViewModel {
        parametersOf(measurement)
    }


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .verticalScroll(state = rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        MeasurementDetailsChartsHeader(
            startTime = viewModel.getMeasurementStartTimeString(),
            duration = viewModel.getMeasurementDurationString(),
            averageLevel = viewModel.measurement.laeqMetrics.average,
        )

        viewModel.measurement.recordedAudioUrl?.let { audioUrl ->
            AudioPlayerView(audioUrl)
        }

        LaeqSummaryView(
            min = viewModel.measurement.laeqMetrics.min,
            la90 = viewModel.measurement.summary?.la90 ?: 0.0,
            la50 = viewModel.measurement.summary?.la50 ?: 0.0,
            la10 = viewModel.measurement.summary?.la10 ?: 0.0,
            max = viewModel.measurement.laeqMetrics.max
        )

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {

        }
    }
}
