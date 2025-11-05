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
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_pager_tab_map
import noisecapture.composeapp.generated.resources.measurement_pager_tab_spectrogram
import noisecapture.composeapp.generated.resources.measurement_pager_tab_spectrum
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.map.MapView
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram.SpectrogramPlotView
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum.SpectrumPlotView

/**
 * A horizontal pager on the measurement recording screens that allows user to switch between
 * spectrum, spectrogram and map views.
 */
@Composable
fun RecordingPager(
    modifier: Modifier = Modifier,
) {

    // - Properties

    val animationScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { TabState.entries.size })

    val tabLabels = mapOf(
        TabState.SPECTRUM to stringResource(Res.string.measurement_pager_tab_spectrum),
        TabState.SPECTROGRAM to stringResource(Res.string.measurement_pager_tab_spectrogram),
        TabState.MAP to stringResource(Res.string.measurement_pager_tab_map),
    )


    // - Layout

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            TabState.entries.forEach { entry ->
                Tab(
                    text = { Text(tabLabels[entry] ?: "") },
                    selected = pagerState.currentPage == entry.ordinal,
                    onClick = { animationScope.launch { pagerState.animateScrollToPage(entry.ordinal) } }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { page ->
            when (TabState.entries[page]) {
                TabState.SPECTROGRAM -> Box {
                    SpectrogramPlotView(
                        modifier = Modifier.padding(end = 16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 80.dp)
                    )
                }

                TabState.SPECTRUM -> Box {
                    SpectrumPlotView(
                        modifier = Modifier.padding(end = 16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 80.dp)
                    )
                }

                TabState.MAP -> Box {
                    MapView(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private enum class TabState {
    SPECTRUM,
    SPECTROGRAM,
    MAP
}
