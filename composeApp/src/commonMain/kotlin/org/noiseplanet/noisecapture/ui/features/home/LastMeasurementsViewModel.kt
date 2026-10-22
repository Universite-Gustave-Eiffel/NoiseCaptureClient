package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import nl.jacobras.humanreadable.HumanReadable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.statistics.UserStatisticsService
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
            val lastMeasurementIds: List<String>,
        ) : ViewState
    }


    // - Properties

    private val measurementService: MeasurementService by inject()
    private val userStatisticsService: UserStatisticsService by inject()

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
                lastMeasurementIds = measurementIds.reversed().take(4)
            )
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState.Loading,
        )
}
