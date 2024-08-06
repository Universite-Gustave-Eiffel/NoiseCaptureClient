//
//
// TODO: Split this file!!!!
//
//
@file:Suppress("TooManyFunctions")

package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.measurements.MeasurementsService
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsView
import org.noiseplanet.noisecapture.ui.features.measurement.spectrogram.SpectrogramPlotView
import org.noiseplanet.noisecapture.ui.features.measurement.spectrum.SpectrumPlotView

const val SPECTROGRAM_STRIP_WIDTH = 32
const val REFERENCE_LEGEND_TEXT = " +99s "
const val DEFAULT_SAMPLE_RATE = 48000.0
const val MIN_SHOWN_DBA_VALUE = 5.0
const val MAX_SHOWN_DBA_VALUE = 140.0

val NOISE_LEVEL_FONT_SIZE = TextUnit(50F, TextUnitType.Sp)
val SPECTRUM_PLOT_SQUARE_WIDTH = 10.dp
val SPECTRUM_PLOT_SQUARE_OFFSET = 1.dp

@OptIn(KoinExperimentalAPI::class)
@Suppress("LargeClass")
class MeasurementScreen(
    private val measurementService: MeasurementsService,
) {

    @OptIn(ExperimentalFoundationApi::class)
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


    @OptIn(ExperimentalFoundationApi::class)
    @Suppress("LongParameterList", "LongMethod")
    @Composable
    fun Content() {

        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> measurementService.startRecordingAudio()
                    Lifecycle.Event.ON_STOP -> measurementService.stopRecordingAudio()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints {
                if (maxWidth > maxHeight) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxWidth(.5F)) {
                            AcousticIndicatorsView(viewModel = koinViewModel())
                        }
                        Column(modifier = Modifier) {
                            MeasurementPager()
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AcousticIndicatorsView(viewModel = koinViewModel())
                        MeasurementPager()
                    }
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
