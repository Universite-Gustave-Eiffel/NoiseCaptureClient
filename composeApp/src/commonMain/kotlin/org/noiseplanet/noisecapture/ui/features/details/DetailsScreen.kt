package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_loading_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerView
import org.noiseplanet.noisecapture.ui.components.map.MapView
import org.noiseplanet.noisecapture.ui.navigation.router.DetailsRouter


@OptIn(KoinExperimentalAPI::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsScreenViewModel,
    router: DetailsRouter,
) {

    // - DI

    rememberKoinModules {
        listOf(detailsModule)
    }


    // - Properties

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass


    // - Layout

    Surface {
        Crossfade(viewState) { viewState ->
            when (viewState) {
                is DetailsScreenViewModel.ViewState.ContentReady -> {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        if (sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) {
                            DetailsScreenCompact(viewState)
                        } else if (sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) {
                            DetailsScreenMedium(viewState)
                        } else {
                            DetailsScreenLarge(viewState)
                        }
                    }
                }

                is DetailsScreenViewModel.ViewState.Loading -> {
                    ContentLoadingView()
                }

                is DetailsScreenViewModel.ViewState.NoMeasurement -> {
                    // If measurement becomes null, it means it was deleted
                    router.onMeasurementDeleted()
                }
            }
        }
    }
}


@Composable
private fun DetailsScreenLarge(
    viewState: DetailsScreenViewModel.ViewState.ContentReady,
) {
    // - Properties

    val scrollState = rememberScrollState()


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.verticalScroll(scrollState)
            .padding(32.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.weight(0.8f),
        ) {
            DetailsChartsHeader(
                startTime = viewState.startTimeString,
                duration = viewState.durationString,
                averageLevel = viewState.measurement.laeqMetrics.average
            )

            viewState.measurement.recordedAudioUrl?.let { audioUrl ->
                AudioPlayerView(audioUrl)
            }

            LaeqSummaryView(
                min = viewState.measurement.laeqMetrics.min,
                la90 = viewState.measurement.summary?.la90 ?: 0.0,
                la50 = viewState.measurement.summary?.la50 ?: 0.0,
                la10 = viewState.measurement.summary?.la10 ?: 0.0,
                max = viewState.measurement.laeqMetrics.max
            )

            ManageMeasurementView(measurementId = viewState.measurement.uuid)
        }

        val summary = viewState.measurement.summary ?: return

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.weight(2f),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                MapViewOrPlaceHolder(viewState, modifier = Modifier.aspectRatio(1.25f).weight(1f))

                RnePlotView(summary.repartitionOfNoiseExposure, modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                if (summary.leqOverTime.isNotEmpty()) {
                    SplTimePlotView(
                        leqOverTime = summary.leqOverTime,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (summary.avgLevelPerFreq.isNotEmpty()) {
                    AverageLevelPerFreqView(
                        avgLevelPerFreq = summary.avgLevelPerFreq,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
private fun DetailsScreenMedium(
    viewState: DetailsScreenViewModel.ViewState.ContentReady,
) {
    // - Properties

    val scrollState = rememberScrollState()


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .padding(24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f),
            ) {
                DetailsChartsHeader(
                    startTime = viewState.startTimeString,
                    duration = viewState.durationString,
                    averageLevel = viewState.measurement.laeqMetrics.average
                )

                viewState.measurement.recordedAudioUrl?.let { audioUrl ->
                    AudioPlayerView(audioUrl)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Spacer(Modifier.weight(1f))
                LaeqSummaryView(
                    min = viewState.measurement.laeqMetrics.min,
                    la90 = viewState.measurement.summary?.la90 ?: 0.0,
                    la50 = viewState.measurement.summary?.la50 ?: 0.0,
                    la10 = viewState.measurement.summary?.la10 ?: 0.0,
                    max = viewState.measurement.laeqMetrics.max
                )
                Spacer(Modifier.weight(1f))
            }
        }

        MapViewOrPlaceHolder(viewState, modifier = Modifier.aspectRatio(2f))

        viewState.measurement.summary?.let { summary ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (summary.leqOverTime.isNotEmpty()) {
                    SplTimePlotView(
                        leqOverTime = summary.leqOverTime,
                        modifier = Modifier.weight(1f),
                    )
                }

                RnePlotView(
                    rneData = summary.repartitionOfNoiseExposure,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            viewState.measurement.summary?.let { summary ->
                if (summary.avgLevelPerFreq.isNotEmpty()) {
                    AverageLevelPerFreqView(
                        avgLevelPerFreq = summary.avgLevelPerFreq,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            ManageMeasurementView(
                measurementId = viewState.measurement.uuid,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun DetailsScreenCompact(
    viewState: DetailsScreenViewModel.ViewState.ContentReady,
) {
    // - Properties

    val scrollState = rememberScrollState()


    // - Layout

    Column(
        modifier = Modifier.verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        DetailsChartsHeader(
            startTime = viewState.startTimeString,
            duration = viewState.durationString,
            averageLevel = viewState.measurement.laeqMetrics.average
        )

        MapViewOrPlaceHolder(viewState, modifier = Modifier.aspectRatio(1.5f))

        viewState.measurement.recordedAudioUrl?.let { audioUrl ->
            AudioPlayerView(audioUrl)
        }

        viewState.measurement.summary?.let { summary ->
            if (summary.leqOverTime.isNotEmpty()) {
                SplTimePlotView(leqOverTime = summary.leqOverTime)
            }

            LaeqSummaryView(
                min = viewState.measurement.laeqMetrics.min,
                la90 = summary.la90,
                la50 = summary.la50,
                la10 = summary.la10,
                max = viewState.measurement.laeqMetrics.max
            )

            RnePlotView(rneData = summary.repartitionOfNoiseExposure)

            if (summary.avgLevelPerFreq.isNotEmpty()) {
                AverageLevelPerFreqView(avgLevelPerFreq = summary.avgLevelPerFreq)
            }

            ManageMeasurementView(
                measurementId = viewState.measurement.uuid,
            )
        }
    }
}


@Composable
private fun ContentLoadingView() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.measurement_details_loading_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}


/**
 * Will show the map if measurement has location data, or a placeholder
 */
@Composable
private fun MapViewOrPlaceHolder(
    viewState: DetailsScreenViewModel.ViewState.ContentReady,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clip(shape = MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (viewState.measurement.locationSequenceIds.isNotEmpty()) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                focusedMeasurementUuid = viewState.measurement.uuid,
            )
        } else {
            Text(
                text = "No location data available\nfor this measurement.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
