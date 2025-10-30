package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapView
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
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            MeasurementTabState.entries.forEach { entry ->
                Tab(
                    text = { Text(MEASUREMENT_TAB_LABEL[entry.ordinal]) },
                    selected = pagerState.currentPage == entry.ordinal,
                    onClick = { animationScope.launch { pagerState.animateScrollToPage(entry.ordinal) } }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { page ->
            when (MeasurementTabState.entries[page]) {
                MeasurementTabState.SPECTROGRAM -> Box {
                    SpectrogramPlotView(
                        modifier = Modifier.padding(end = 16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 80.dp)
                    )
                }

                MeasurementTabState.SPECTRUM -> Box {
                    SpectrumPlotView(
                        modifier = Modifier.padding(end = 16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 80.dp)
                    )
                }

                MeasurementTabState.MAP -> Box {
                    MeasurementsMapView(modifier = Modifier.fillMaxSize())
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
