package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_open_history_button_title
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.statistics.UserStatisticsService
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed
import kotlin.time.Duration.Companion.milliseconds


class LastMeasurementsViewModel : ViewModel(), KoinComponent {

    // - States

    sealed interface ViewState {

        data object Loading : ViewState

        data class ContentReady(
            val measurementsCount: Int,
            val totalDuration: String,
            val durationUnit: String,
            val historyButtonViewModel: NCButtonViewModel,
            val lastMeasurementIds: List<String>,
        ) : ViewState
    }


    // - Properties

    private val measurementService: MeasurementService by inject()
    private val userStatisticsService: UserStatisticsService by inject()

    private val openHistoryButtonViewModel = NCButtonViewModel(
        title = Res.string.home_open_history_button_title,
        style = NCButtonStyle.OUTLINED,
        colors = { NCButtonColors.Defaults.outlined() },
        icon = Icons.Default.History,
    )

    val viewStateFlow: StateFlow<ViewState> = measurementService
        .getAllMeasurementIdsFlow()
        .map { measurementIds ->
            val statistics = userStatisticsService.get()
            val durationString = HumanReadable.duration(
                statistics.totalMeasuredDuration.milliseconds
            )
            val (durationValue, durationUnit) = durationString.split(" ")

            ViewState.ContentReady(
                measurementsCount = statistics.totalMeasurementsCount,
                totalDuration = durationValue,
                durationUnit = durationUnit,
                historyButtonViewModel = openHistoryButtonViewModel,
                lastMeasurementIds = measurementIds.reversed().take(4)
            )
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState.Loading,
        )
}
