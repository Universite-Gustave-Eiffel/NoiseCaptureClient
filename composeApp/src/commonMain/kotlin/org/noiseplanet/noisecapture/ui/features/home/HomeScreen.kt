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
import org.noiseplanet.noisecapture.model.dao.Measurement

/**
 * Home screen layout.
 */
//@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
) {
    // - Properties

    val measurements = remember { mutableStateListOf<Measurement>() }

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
            SoundLevelMeterHeaderView(viewModel)

            if (measurements.isNotEmpty()) {
                LastMeasurementsView(viewModel.lastMeasurementsViewModel)
            }

            // TODO: Add device calibration section

            // TODO: Add more info section
        }
    }
}
