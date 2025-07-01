package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_open_history_button_title
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class LastMeasurementsViewModel : ViewModel(), KoinComponent {

    // - States

    sealed interface ViewState {

        data object Loading : ViewState

        data class ContentReady(
            val measurementsCount: Int,
            val totalDuration: String,
            val durationUnit: String,
            val historyButtonViewModel: NCButtonViewModel,
            val lastTwoMeasurements: List<Measurement>,
        ) : ViewState
    }


    // - Properties

    private val measurementService: MeasurementService by inject()

    private val openHistoryButtonViewModel = NCButtonViewModel(
        title = Res.string.home_open_history_button_title,
        style = NCButtonStyle.OUTLINED,
        colors = { NCButtonColors.Defaults.outlined() },
        icon = Icons.Default.History,
    )

    val viewStateFlow: StateFlow<ViewState> = measurementService
        .getAllMeasurementsFlow()
        .map { measurements ->
            val durationMilliseconds = getMeasurementsTotalDuration(measurements)
            val durationString = HumanReadable.duration(durationMilliseconds)
            val (durationValue, durationUnit) = durationString.split(" ")

            ViewState.ContentReady(
                measurementsCount = measurements.size,
                totalDuration = durationValue,
                durationUnit = durationUnit,
                historyButtonViewModel = openHistoryButtonViewModel,
                lastTwoMeasurements = measurements.sortedByDescending { it.startTimestamp }.take(2)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState.Loading,
        )


    // - Private functions

    private fun getMeasurementsTotalDuration(measurements: List<Measurement>): Duration {
        return if (measurements.isNotEmpty()) {
            measurements.map { it.duration }
                .reduce { total, duration -> total + duration }
                .toDuration(unit = DurationUnit.MILLISECONDS)
        } else {
            Duration.ZERO
        }
    }
}
