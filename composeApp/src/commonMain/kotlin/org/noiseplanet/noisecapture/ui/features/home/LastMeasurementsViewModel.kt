package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_open_history_button_title
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.ButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.ButtonViewModel
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class LastMeasurementsViewModel(
    val onClickOpenHistoryButton: () -> Unit,
    val onClickMeasurement: (Measurement) -> Unit,
) : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()
    private val measurementsFlow: Flow<List<Measurement>> =
        measurementService.getAllMeasurementsFlow()

    val lastTwoMeasurementsFlow: Flow<List<Measurement>> = measurementsFlow.map { measurements ->
        measurements.sortedByDescending { it.startTimestamp }
            .take(2)
    }

    val measurementsCountFlow: Flow<Int> = measurementsFlow.map { measurements ->
        measurements.size
    }

    val totalMeasurementsDurationFlow: Flow<Duration> = measurementsFlow.map { measurements ->
        if (measurements.isNotEmpty()) {
            measurements.map { it.duration }
                .reduce { total, duration -> total + duration }
                .toDuration(unit = DurationUnit.MILLISECONDS)
        } else {
            Duration.ZERO
        }
    }

    val openHistoryButtonViewModel = ButtonViewModel(
        onClick = onClickOpenHistoryButton,
        title = Res.string.home_open_history_button_title,
        style = ButtonStyle.OUTLINED,
        icon = Icons.Default.History,
    )
}
