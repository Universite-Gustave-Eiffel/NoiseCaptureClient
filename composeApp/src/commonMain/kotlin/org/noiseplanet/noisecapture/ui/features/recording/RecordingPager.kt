package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_pager_tab_map
import noisecapture.composeapp.generated.resources.measurement_pager_tab_spectrogram
import noisecapture.composeapp.generated.resources.measurement_pager_tab_spectrum
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.ui.components.map.MapView
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram.SpectrogramPlotView
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum.SpectrumPlotView
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.navigationBarInsetsTop
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets

/**
 * A horizontal pager on the measurement recording screens that allows user to switch between
 * spectrum, spectrogram and map views.
 */
@Composable
fun RecordingPager(
    locationService: UserLocationService = koinInject(),
    modifier: Modifier = Modifier,
) {

    // - Properties

    val isLocationAvailable: Boolean by locationService.isLocationAvailable
        .collectAsStateWithLifecycle(initialValue = true)
    val showMapBadge: Boolean by locationService.isSignalPoor
        .combine(locationService.isLocationAvailable) { isSignalPoor, isLocationAvailable ->
            isSignalPoor || !isLocationAvailable
        }.collectAsStateWithLifecycle(initialValue = false)

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND

    var tabs = mapOf(
        TabId.SPECTRUM to stringResource(Res.string.measurement_pager_tab_spectrum),
        TabId.SPECTROGRAM to stringResource(Res.string.measurement_pager_tab_spectrogram),
    )

    // Only show the map tab on compact screens. On larger screens, map is always visible.
    if (isCompact) {
        tabs = tabs + mapOf(
            TabId.MAP to stringResource(Res.string.measurement_pager_tab_map)
        )
    }
    val pagePaddingModifier = if (isCompact) {
        Modifier.navigationBarInsetsTop()
            .paddingBottomWithInsets(80.dp) // 64dp for recording controls + 16dp of spacing
            .padding(end = 16.dp)
    } else {
        Modifier.padding(end = 16.dp, bottom = 16.dp)
    }

    val animationScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabs.entries.size })


    // - Layout

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            tabs.toList().forEachIndexed { index, (tabId, label) ->
                Tab(
                    text = {
                        BadgedBox(
                            badge = {
                                if (tabId == TabId.MAP && showMapBadge) {
                                    Badge(
                                        contentColor = NoiseLevelColorRamp.level8,
                                        modifier = Modifier.offset(x = 8.dp)
                                    )
                                }
                            }
                        ) {
                            BasicText(
                                text = label,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 12.sp,
                                    maxFontSize = 16.sp,
                                    stepSize = 0.25.sp,
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                            )
                        }
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        animationScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { page ->
            when (TabId.entries[page]) {
                TabId.SPECTROGRAM -> Box {
                    SpectrogramPlotView(
                        modifier = pagePaddingModifier
                    )
                }

                TabId.SPECTRUM -> Box {
                    SpectrumPlotView(
                        modifier = pagePaddingModifier
                    )
                }

                TabId.MAP -> Box {
                    MapView(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private enum class TabId {
    SPECTRUM,
    SPECTROGRAM,
    MAP
}
