package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.details_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.util.DateUtil
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
class DetailsScreenViewModel(
    val measurementId: String,
) : ViewModel(), ScreenViewModel, KoinComponent {

    // - ViewState

    sealed interface ViewState {

        data class ContentReady(
            val measurement: Measurement,
            val startTimeString: String,
            val durationString: String,
            val audioFilePath: String?,
        ) : ViewState

        data object Loading : ViewState
        data object NoMeasurement : ViewState
    }


    // - Properties

    private val fileSystemService: FileSystemService by inject()
    private val measurementService: MeasurementService by inject()

    private val measurementFlow = measurementService.getMeasurementFlow(measurementId)
        .map { measurement ->
            // If measurement has no summary yet, we need to calculate it.
            // The view should display a loading state until measurement is ready.
            if (measurement != null && measurement.summary == null) {
                measurementService.calculateSummary(measurement)
            } else {
                measurement
            }
        }

    val viewState: StateFlow<ViewState> = measurementFlow
        .map { measurement ->
            measurement?.let {
                ViewState.ContentReady(
                    measurement = it,
                    startTimeString = getMeasurementStartTimeString(measurement),
                    durationString = getMeasurementDurationString(measurement),
                    audioFilePath = getMeasurementAudioFilePath(measurement)
                )
            } ?: ViewState.NoMeasurement
        }
        .stateInWhileSubscribed(
            viewModelScope,
            initialValue = ViewState.Loading
        )


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.details_title


    // - Private functions

    private fun getMeasurementStartTimeString(measurement: Measurement): String {
        val instant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return localDateTime.format(DateUtil.Format.MEASUREMENT_START_DATETIME)
    }

    private fun getMeasurementDurationString(measurement: Measurement): String {
        val startInstant = Instant.fromEpochMilliseconds(measurement.startTimestamp)
        val endInstant = Instant.fromEpochMilliseconds(measurement.endTimestamp)
        val duration = endInstant - startInstant

        return duration.toComponents { hours, minutes, seconds, _ ->
            "${hours}h ${minutes}m ${seconds}s"
        }
    }

    private fun getMeasurementAudioFilePath(measurement: Measurement): String? {
        return measurement.recordedAudioFileName?.let {
            fileSystemService.getAudioFileAbsolutePath(it)
        }
    }
}
