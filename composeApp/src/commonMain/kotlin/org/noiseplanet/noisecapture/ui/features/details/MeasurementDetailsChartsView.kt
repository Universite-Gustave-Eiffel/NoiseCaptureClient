package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement


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
        modifier = modifier.fillMaxWidth().padding(bottom = 48.dp)
    ) {
        MeasurementDetailsChartsHeader(
            startTime = viewModel.getMeasurementStartTimeString(),
            duration = viewModel.getMeasurementDurationString(),
            averageLevel = viewModel.measurement.laeqMetrics.average,
        )

        LaeqSummaryView(
            min = viewModel.measurement.laeqMetrics.min,
            la90 = viewModel.measurement.summary?.la90 ?: 0.0,
            la50 = viewModel.measurement.summary?.la50 ?: 0.0,
            la10 = viewModel.measurement.summary?.la10 ?: 0.0,
            max = viewModel.measurement.laeqMetrics.max
        )
    }
}
