//
//
// TODO: Split this file!!!!
//
//
@file:Suppress("TooManyFunctions")

package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.audio.ANDROID_GAIN
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumData
import org.noiseplanet.noisecapture.measurements.DefaultMeasurementService.Companion.FFT_HOP
import org.noiseplanet.noisecapture.measurements.MeasurementsService
import org.noiseplanet.noisecapture.ui.components.measurement.LegendElement
import org.noiseplanet.noisecapture.ui.components.measurement.SpectrogramBitmap
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsView
import org.noiseplanet.noisecapture.ui.features.measurement.spectrum.SpectrumPlotView
import org.noiseplanet.noisecapture.util.toImageBitmap
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

const val SPECTROGRAM_STRIP_WIDTH = 32
const val REFERENCE_LEGEND_TEXT = " +99s "
const val DEFAULT_SAMPLE_RATE = 48000.0
const val MIN_SHOWN_DBA_VALUE = 5.0
const val MAX_SHOWN_DBA_VALUE = 140.0

val NOISE_LEVEL_FONT_SIZE = TextUnit(50F, TextUnitType.Sp)
val SPECTRUM_PLOT_SQUARE_WIDTH = 10.dp
val SPECTRUM_PLOT_SQUARE_OFFSET = 1.dp

// TODO: Refactor this screen
@OptIn(KoinExperimentalAPI::class)
@Suppress("LargeClass")
class MeasurementScreen(
    private val measurementService: MeasurementsService,
) {

    private var rangedB = 40.0
    private var mindB = 0.0
    private var dbGain = ANDROID_GAIN

    companion object {

        fun timeAxisFormater(timeValue: Double): String {
            return "+${round(timeValue).toInt()}s"
        }

        fun noiseLevelAxisFormater(timeValue: Double): String {
            return "${round(timeValue).toInt()} dB"
        }

        val tickStroke = 2.dp
        val tickLength = 4.dp

        // TODO: Cleanup legend generation functions
        @Suppress("LongParameterList")
        fun makeXLegend(
            textMeasurer: TextMeasurer,
            xValue: Double,
            legendWidth: Float,
            xPerPixel: Double,
            depth: Int,
            formater: (x: Double) -> String,
            ascending: Boolean,
        ): LegendElement {
            val xPos =
                when {
                    ascending -> (xValue / xPerPixel).toFloat()
                    else -> (legendWidth - xValue / xPerPixel).toFloat()
                }
            val legendText = buildAnnotatedString {
                withStyle(style = SpanStyle()) {
                    append(formater(xValue))
                }
            }
            val textLayout = textMeasurer.measure(legendText)
            val textPos = min(
                legendWidth - textLayout.size.width,
                max(0F, xPos - textLayout.size.width / 2)
            )
            return LegendElement(textLayout, xPos, textPos, depth)
        }

        // TODO: Cleanup legend generation functions
        @Suppress("LongParameterList")
        fun recursiveLegendBuild(
            textMeasurer: TextMeasurer,
            timeValue: Double,
            legendWidth: Float,
            timePerPixel: Double,
            minPixel: Float,
            maxPixel: Float,
            xLeftValue: Double,
            xRightValue: Double,
            feedElements: ArrayList<LegendElement>,
            depth: Int,
            formater: (x: Double) -> String,
        ) {
            val legendElement =
                makeXLegend(
                    textMeasurer,
                    timeValue,
                    legendWidth,
                    timePerPixel,
                    depth,
                    formater,
                    xLeftValue < xRightValue
                )
            // Add sub axis element if the text does not overlap with neighboring texts
            if (legendElement.textPos > minPixel && legendElement.xPos + legendElement.text.size.width / 2 < maxPixel) {
                feedElements.add(legendElement)
                // left legend, + x seconds
                recursiveLegendBuild(
                    textMeasurer,
                    xLeftValue + (timeValue - xLeftValue) / 2,
                    legendWidth,
                    timePerPixel,
                    minPixel,
                    legendElement.textPos,
                    xLeftValue,
                    timeValue,
                    feedElements,
                    depth + 1,
                    formater
                )
                // right legend, - x seconds
                recursiveLegendBuild(
                    textMeasurer,
                    timeValue + (xRightValue - timeValue) / 2,
                    legendWidth,
                    timePerPixel,
                    legendElement.textPos + legendElement.text.size.width,
                    maxPixel,
                    timeValue,
                    xRightValue,
                    feedElements,
                    depth + 1,
                    formater
                )
            }
        }

        fun makeXLabels(
            textMeasurer: TextMeasurer,
            leftValue: Double,
            rightValue: Double,
            xLegendWidth: Float,
            formater: (x: Double) -> String,
        ): ArrayList<LegendElement> {
            val xPerPixel = abs(leftValue - rightValue) / xLegendWidth
            val legendElements = ArrayList<LegendElement>()
            val leftLegend =
                makeXLegend(
                    textMeasurer,
                    leftValue,
                    xLegendWidth,
                    xPerPixel,
                    -1,
                    formater,
                    leftValue < rightValue
                )
            val rightLegend =
                makeXLegend(
                    textMeasurer,
                    rightValue,
                    xLegendWidth,
                    xPerPixel,
                    -1,
                    formater,
                    leftValue < rightValue
                )
            legendElements.add(leftLegend)
            legendElements.add(rightLegend)
            // Add axis texts between left and rightmost axis texts (until it overlaps)
            recursiveLegendBuild(
                textMeasurer,
                abs(leftValue - rightValue) / 2,
                xLegendWidth,
                xPerPixel,
                leftLegend.text.size.width.toFloat(),
                rightLegend.xPos - rightLegend.text.size.width,
                leftValue,
                rightValue,
                legendElements,
                0,
                formater
            )
            // find depth index with maximum number of elements (to generate same intervals on legend)
            val legendDepthCount = IntArray(legendElements.maxOf { it.depth } + 1) { 0 }
            legendElements.forEach {
                if (it.depth >= 0) {
                    legendDepthCount[it.depth] += 1
                }
            }
            // remove sub-axis texts with isolated depth (should produce same intervals between axis text)
            legendElements.removeAll {
                it.depth > 0 && legendDepthCount[it.depth] != (2.0.pow(it.depth)).toInt()
            }
            return legendElements
        }
    }


    private val scaleMode = SpectrogramBitmap.ScaleMode.SCALE_LOG
    val spectrumCanvasState = SpectrogramViewModel(
        SpectrogramBitmap.SpectrogramDataModel(
            IntSize(1, 1),
            ByteArray(Int.SIZE_BYTES), 0, SpectrogramBitmap.ScaleMode.SCALE_LOG, 1.0
        ), ArrayList(), Size.Zero
    )

    var preparedSpectrogramOverlayBitmap =
        PlotBitmapOverlay(ImageBitmap(1, 1), Size(0F, 0F), Size(0F, 0F), Size(0F, 0F), 0)


    @Composable
    fun spectrogram(spectrumCanvasState: SpectrogramViewModel, bitmapOffset: Int) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize =
                IntSize(
                    SPECTROGRAM_STRIP_WIDTH,
                    (size.height - preparedSpectrogramOverlayBitmap.horizontalLegendSize.height).toInt()
                )
            drawRect(
                color = SpectrogramBitmap.colorRamp[0],
                size = Size(
                    size.width - preparedSpectrogramOverlayBitmap.verticalLegendSize.width,
                    canvasSize.height.toFloat()
                )
            )
            spectrumCanvasState.spectrogramCanvasSize = Size(
                size.width - preparedSpectrogramOverlayBitmap.verticalLegendSize.width, size.height
                    - preparedSpectrogramOverlayBitmap.horizontalLegendSize.height
            )
            if (spectrumCanvasState.currentStripData.size.height != canvasSize.height) {
                // reset buffer on resize or first draw
                println(
                    "Clear ${spectrumCanvasState.cachedStrips.size} strips " +
                        "${spectrumCanvasState.currentStripData.size.height} != ${canvasSize.height}"
                )
                spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(
                    canvasSize, scaleMode, spectrumCanvasState.currentStripData.sampleRate
                )
                spectrumCanvasState.cachedStrips.clear()
            } else {
                if (spectrumCanvasState.currentStripData.sampleRate > 1) {
                    drawImage(
                        spectrumCanvasState.currentStripData.byteArray.toImageBitmap(),
                        topLeft = Offset(
                            size.width - bitmapOffset - preparedSpectrogramOverlayBitmap.verticalLegendSize.width,
                            0F
                        )
                    )
                    spectrumCanvasState.cachedStrips.reversed()
                        .forEachIndexed { index, imageBitmap ->
                            val bitmapX = size.width -
                                preparedSpectrogramOverlayBitmap.verticalLegendSize.width -
                                ((index + 1) * SPECTROGRAM_STRIP_WIDTH + bitmapOffset).toFloat()
                            drawImage(
                                imageBitmap,
                                topLeft = Offset(bitmapX, 0F)
                            )
                        }
                }
            }
        }
    }

    @Suppress("LongParameterList", "LongMethod")
    fun buildSpectrogramAxisBitmap(
        size: Size,
        density: Density,
        scaleMode: SpectrogramBitmap.ScaleMode,
        sampleRate: Double,
        textMeasurer: TextMeasurer,
        colors: ColorScheme,
    ): PlotBitmapOverlay {
        val drawScope = CanvasDrawScope()
        val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
        val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

        var frequencyLegendPosition = when (scaleMode) {
            SpectrogramBitmap.ScaleMode.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
            else -> SpectrogramBitmap.frequencyLegendPositionLinear
        }
        frequencyLegendPosition =
            frequencyLegendPosition.filter { f -> f < sampleRate / 2 }.toIntArray()
        val timeXLabelMeasure = textMeasurer.measure(REFERENCE_LEGEND_TEXT)
        val timeXLabelHeight = timeXLabelMeasure.size.height
        val maxYLabelWidth =
            frequencyLegendPosition.maxOf { frequency ->
                val text = formatFrequency(frequency)
                textMeasurer.measure(text).size.width
            }
        var bottomLegendSize = Size(0F, 0F)
        var rightLegendSize = Size(0F, 0F)
        drawScope.draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = size,
        ) {
            val legendHeight = timeXLabelHeight + tickLength.toPx()
            val legendWidth = maxYLabelWidth + tickLength.toPx()
            bottomLegendSize = Size(size.width - legendWidth, legendHeight)
            rightLegendSize = Size(legendWidth, size.height - legendHeight)
            if (sampleRate > 1) {
                // draw Y axe labels
                val fMax = sampleRate / 2
                val fMin = frequencyLegendPosition[0].toDouble()
                val sheight = (size.height - legendHeight).toInt()
                frequencyLegendPosition.forEachIndexed { index, frequency ->
                    val text = buildAnnotatedString {
                        withStyle(style = SpanStyle()) {
                            append(formatFrequency(frequency))
                        }
                    }
                    val textSize = textMeasurer.measure(text)
                    val tickHeightPos = when (scaleMode) {
                        SpectrogramBitmap.ScaleMode.SCALE_LOG -> {
                            sheight - (log10(frequency / fMin) / ((log10(fMax / fMin) / sheight))).toInt()
                        }

                        else -> (sheight - frequency / fMax * sheight).toInt()
                    }
                    drawLine(
                        color = colors.onSurfaceVariant, start = Offset(
                            size.width - legendWidth,
                            tickHeightPos.toFloat() - tickStroke.toPx() / 2
                        ),
                        end = Offset(
                            size.width - legendWidth + tickLength.toPx(),
                            tickHeightPos.toFloat() - tickStroke.toPx() / 2
                        ),
                        strokeWidth = tickStroke.toPx()
                    )
                    val textPos = min(
                        (size.height - textSize.size.height).toInt(),
                        max(0, tickHeightPos - textSize.size.height / 2)
                    )
                    drawText(
                        textMeasurer,
                        text,
                        topLeft = Offset(
                            size.width - legendWidth + tickLength.toPx(),
                            textPos.toFloat()
                        )
                    )
                }
                val xLegendWidth = (size.width - legendWidth)
                val legendElements = makeXLabels(
                    textMeasurer, (FFT_HOP / sampleRate) * xLegendWidth, 0.0,
                    xLegendWidth, Companion::timeAxisFormater
                )
                legendElements.forEach { legendElement ->
                    val tickPos =
                        max(
                            tickStroke.toPx() / 2F,
                            min(
                                xLegendWidth - tickStroke.toPx(),
                                legendElement.xPos - tickStroke.toPx() / 2F
                            )
                        )
                    drawLine(
                        color = colors.onSurfaceVariant, start = Offset(
                            tickPos,
                            sheight.toFloat()
                        ),
                        end = Offset(
                            tickPos,
                            sheight + tickLength.toPx()
                        ),
                        strokeWidth = tickStroke.toPx()
                    )
                    drawText(
                        legendElement.text,
                        topLeft = Offset(
                            legendElement.textPos,
                            sheight.toFloat() + tickLength.toPx()
                        )
                    )
                }
            }
        }
        return PlotBitmapOverlay(
            bitmap,
            size,
            bottomLegendSize,
            rightLegendSize,
            scaleMode.hashCode()
        )
    }

    @Composable
    fun spectrogramAxis(scaleMode: SpectrogramBitmap.ScaleMode, sampleRate: Double) {
        val colors = MaterialTheme.colorScheme
        val textMeasurer = rememberTextMeasurer()
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (preparedSpectrogramOverlayBitmap.imageSize != size) {
                preparedSpectrogramOverlayBitmap = buildSpectrogramAxisBitmap(
                    size, Density(density), scaleMode,
                    sampleRate, textMeasurer, colors
                )
            }
            drawImage(preparedSpectrogramOverlayBitmap.imageBitmap)
        }
    }

    fun processSpectrum(spectrumCanvasState: SpectrogramViewModel, it: SpectrumData): Int {
        spectrumCanvasState.currentStripData.pushSpectrumToSpectrogramData(
            it, mindB, rangedB, dbGain
        )
        if (spectrumCanvasState.currentStripData.offset == SPECTROGRAM_STRIP_WIDTH) {
            // spectrogram band complete, store bitmap
            spectrumCanvasState.cachedStrips.add(
                spectrumCanvasState.currentStripData.byteArray.toImageBitmap()
            )
            if ((spectrumCanvasState.cachedStrips.size - 1) *
                SPECTROGRAM_STRIP_WIDTH >
                spectrumCanvasState.spectrogramCanvasSize.width
            ) {
                // remove offscreen bitmaps
                spectrumCanvasState.cachedStrips.removeAt(0)
            }
            spectrumCanvasState.currentStripData =
                SpectrogramBitmap.createSpectrogram(
                    spectrumCanvasState.currentStripData.size,
                    spectrumCanvasState.currentStripData.scaleMode,
                    it.sampleRate.toDouble()
                )
        }
        return spectrumCanvasState.currentStripData.offset
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun measurementPager(
        bitmapOffset: Int,
        sampleRate: Double,
    ) {

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
                        spectrogram(spectrumCanvasState, bitmapOffset)
                        spectrogramAxis(scaleMode, sampleRate)
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
        var bitmapOffset by remember { mutableStateOf(0) }
        var sampleRate by remember { mutableStateOf(DEFAULT_SAMPLE_RATE) }

        val lifecycleOwner = LocalLifecycleOwner.current

        var spectrumCollectJob: Job? = null
        val launchMeasurementJob = fun() {
            spectrumCollectJob = lifecycleOwner.lifecycleScope.launch {
                println("Launch spectrum lifecycle")
                measurementService.getSpectrumDataFlow().collect() { spectrumData ->
                    sampleRate = spectrumData.sampleRate.toDouble()
                    if (spectrumCanvasState.currentStripData.size.width > 1) {
                        bitmapOffset = processSpectrum(spectrumCanvasState, spectrumData)
                    }
                }
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> measurementService.startRecordingAudio()
                    Lifecycle.Event.ON_STOP -> measurementService.stopRecordingAudio()
                    Lifecycle.Event.ON_PAUSE -> {
                        spectrumCollectJob?.cancel()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        launchMeasurementJob()
                    }

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
                            measurementPager(
                                bitmapOffset,
                                sampleRate
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AcousticIndicatorsView(viewModel = koinViewModel())
                        measurementPager(
                            bitmapOffset,
                            sampleRate
                        )
                    }
                }
            }
        }
    }
}

data class SpectrogramViewModel(
    var currentStripData: SpectrogramBitmap.SpectrogramDataModel,
    val cachedStrips: ArrayList<ImageBitmap>,
    var spectrogramCanvasSize: Size,
)

enum class MeasurementTabState { SPECTRUM,
    SPECTROGRAM,
    MAP
}

val MEASUREMENT_TAB_LABEL = listOf("Spectrum", "Spectrogram", "Map")

data class PlotBitmapOverlay(
    val imageBitmap: ImageBitmap,
    val imageSize: Size,
    val horizontalLegendSize: Size,
    val verticalLegendSize: Size,
    val plotSettingsHashCode: Int,
)

data class MeasurementStatistics(val label: String, val value: String)

fun formatFrequency(frequency: Int): String {
    return if (frequency >= 1000) {
        if (frequency % 1000 > 0) {
            val subKilo = (frequency % 1000).toString().trimEnd('0')
            "${frequency / 1000}.$subKilo kHz"
        } else {
            "${frequency / 1000} kHz"
        }
    } else {
        "$frequency Hz"
    }
}
