package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram.SpectrogramPlotView
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum.SpectrumPlotView

/**
 * A horizontal pager on the measurement recording screens that allows user to switch between
 * spectrum, spectrogram and map views.
 */
@Composable
fun MeasurementRecordingPager(
    modifier: Modifier = Modifier,
) {

    // - Properties

    val animationScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { MeasurementTabState.entries.size })


    // - Layout

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
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
                MeasurementTabState.SPECTROGRAM -> Box {
                    SpectrogramPlotView(
                        viewModel = koinViewModel(),
                    )
                }

                MeasurementTabState.SPECTRUM -> Box {
                    SpectrumPlotView(
                        viewModel = koinViewModel(),
                    )
                }

                else -> Surface(
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
