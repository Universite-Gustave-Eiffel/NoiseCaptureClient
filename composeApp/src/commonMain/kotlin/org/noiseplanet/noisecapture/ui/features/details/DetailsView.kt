package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerView
import org.noiseplanet.noisecapture.util.AdaptiveUtil


@Composable
fun DetailsView(
    measurementId: String,
    contentPaddingTop: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: DetailsViewModel = koinViewModel {
        parametersOf(measurementId)
    }
    val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()


    // - Layout

    Crossfade(viewState, modifier = modifier) { viewState ->
        if (viewState is DetailsViewModel.ViewState.ContentReady) {
            CompositionLocalProvider(
                // Disable overscroll on this view so that scrolling up past the limit starts
                // dismissing the bottom sheet instead of bouncing back on iOS.
                LocalOverscrollFactory provides null
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier.widthIn(max = AdaptiveUtil.MAX_FULL_SCREEN_WIDTH)
                            .padding(bottom = 32.dp, top = contentPaddingTop)
                            .windowInsetsPadding(
                                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                            )
                    ) {
                        DetailsChartsHeader(
                            startTime = viewState.startTimeString,
                            duration = viewState.durationString,
                            averageLevel = viewState.measurement.laeqMetrics.average,
                        )

                        viewState.measurement.recordedAudioUrl?.let { audioUrl ->
                            AudioPlayerView(audioUrl)
                        }

                        SplTimePlotView(measurementId)

                        LaeqSummaryView(
                            min = viewState.measurement.laeqMetrics.min,
                            la90 = viewState.measurement.summary?.la90 ?: 0.0,
                            la50 = viewState.measurement.summary?.la50 ?: 0.0,
                            la10 = viewState.measurement.summary?.la10 ?: 0.0,
                            max = viewState.measurement.laeqMetrics.max
                        )

                        ManageMeasurementView(viewState.measurement.uuid)
                    }
                }
            }
        }
    }
}
