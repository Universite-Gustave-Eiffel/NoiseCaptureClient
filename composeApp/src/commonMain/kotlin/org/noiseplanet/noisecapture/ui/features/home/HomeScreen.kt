package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.model.dao.Measurement

/**
 * Home screen layout.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onClickMeasurement: (Measurement) -> Unit,
    onClickOpenHistoryButton: () -> Unit,
    onClickOpenSoundLevelMeterButton: () -> Unit,
) {
    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(homeModule)
    }


    // - Properties

    val measurements = remember { mutableStateListOf<Measurement>() }
    val lastMeasurementsViewModel: LastMeasurementsViewModel = koinViewModel()

    LaunchedEffect(viewModel.viewModelScope) {
        measurements.clear()
        measurements.addAll(viewModel.getStoredMeasurements())
    }


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            SoundLevelMeterHeaderView(
                viewModel = viewModel,
                onClickOpenSoundLevelMeterButton = onClickOpenSoundLevelMeterButton
            )

            if (measurements.isNotEmpty()) {
                LastMeasurementsView(
                    viewModel = lastMeasurementsViewModel,
                    onClickMeasurement = onClickMeasurement,
                    onClickOpenHistoryButton = onClickOpenHistoryButton,
                )
            }

            // TODO: Add device calibration section

            // TODO: Add more info section
        }
    }
}
