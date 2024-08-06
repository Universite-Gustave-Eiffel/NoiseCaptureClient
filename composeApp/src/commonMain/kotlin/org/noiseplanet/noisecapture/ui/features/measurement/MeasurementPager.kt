package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramPlotView
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum.SpectrumPlotView

@OptIn(ExperimentalFoundationApi::class, KoinExperimentalAPI::class)
@Composable
fun MeasurementPager() {
    val animationScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { MeasurementTabState.entries.size })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            MeasurementTabState.entries.forEach { entry ->
                Tab(
                    text = { Text(MEASUREMENT_TAB_LABEL[entry.ordinal]) },
                    selected = pagerState.currentPage == entry.ordinal,
                    onClick = { animationScope.launch { pagerState.animateScrollToPage(entry.ordinal) } }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            when (MeasurementTabState.entries[page]) {
                MeasurementTabState.SPECTROGRAM -> Box(Modifier.fillMaxSize()) {
                    SpectrogramPlotView(
                        viewModel = koinViewModel(),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                MeasurementTabState.SPECTRUM -> Box(Modifier.fillMaxSize()) {
                    SpectrumPlotView(
                        viewModel = koinViewModel(),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = "Text tab ${MEASUREMENT_TAB_LABEL[page]} selected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

enum class MeasurementTabState {
    SPECTRUM,
    SPECTROGRAM,
    MAP
}

val MEASUREMENT_TAB_LABEL = listOf("Spectrum", "Spectrogram", "Map")
