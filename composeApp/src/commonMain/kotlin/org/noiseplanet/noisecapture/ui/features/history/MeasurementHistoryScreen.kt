package org.noiseplanet.noisecapture.ui.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    LazyColumn(
        contentPadding = WindowInsets.safeContent.only(
            WindowInsetsSides.Bottom
        ).add(
            WindowInsets(left = 16.dp, right = 16.dp, top = 16.dp)
        ).asPaddingValues(),
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    ) {

        itemsIndexed(measurements) { index, measurement ->
            val isFirstInSection = index == 0
            val isLastInSection = index == measurements.size - 1

            MeasurementHistoryItemView(
                measurement = measurement,
                onClick = onClickMeasurement,
                isFirstInSection = isFirstInSection,
                isLastInSection = isLastInSection,
            )

            if (!isLastInSection) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}
