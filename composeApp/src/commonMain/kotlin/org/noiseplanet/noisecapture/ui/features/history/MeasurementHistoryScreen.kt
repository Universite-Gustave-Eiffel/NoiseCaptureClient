package org.noiseplanet.noisecapture.ui.features.history

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.model.dao.Measurement
import kotlin.time.ExperimentalTime

@OptIn(FormatStringsInDatetimeFormats::class, KoinExperimentalAPI::class, ExperimentalTime::class)
@Composable
fun MeasurementHistoryScreen(
    viewModel: MeasurementHistoryScreenViewModel,
    onClickMeasurement: (Measurement) -> Unit,
) {

    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(historyModule)
    }


    // - Properties

    val measurements by viewModel.measurementsFlow.collectAsStateWithLifecycle()


    // - Layout

    LazyColumn {

        items(measurements) { measurement ->

            MeasurementHistoryItemView(
                measurement = measurement,
                onClick = onClickMeasurement,
            )
        }
    }
}
