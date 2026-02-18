package org.noiseplanet.noisecapture.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.history_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class HistoryScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()

    val measurementIdsFlow: StateFlow<List<String>> = measurementService.getAllMeasurementIdsFlow()
        .map {
            // Most recent measurements comes first
            it.reversed()
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = emptyList(),
        )


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.history_title
}
