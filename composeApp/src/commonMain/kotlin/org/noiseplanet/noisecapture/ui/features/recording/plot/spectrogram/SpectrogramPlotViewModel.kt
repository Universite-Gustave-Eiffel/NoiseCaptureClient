package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.signal.window.SpectrogramData
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.enums.SpectrogramScaleMode
import org.noiseplanet.noisecapture.services.audio.DefaultLiveAudioService.Companion.FFT_SIZE
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.ui.theme.SpectrogramColorRamp
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.toFrequencyString
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


class SpectrogramPlotViewModel : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        private const val RANGE_DB = 100.0
        private const val MIN_DB = 0.0

        // TODO: Platform dependant gain?
        private const val DB_GAIN = AcousticIndicatorsProcessing.ANDROID_GAIN

        val X_AXIS_TICKS: List<String> = listOf<Int>(20, 15, 10, 5, 0).map { "${it}s" }

        // TODO: Clean this up
        val frequencyLegendPositionLog = intArrayOf(
            63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000, 24000
        )
        val frequencyLegendPositionLinear = IntArray(24) { it * 1000 + 1000 }
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val settingsService: UserSettingsService by inject()
    private val logger: Logger by injectLogger()

    private var canvasSize: IntSize = IntSize.Zero
    private var canvasDensity: Density = Density(1f)

    private var currentBitmap: ImageBitmap? = null
    private val _bitmapFlow: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    val bitmapFlow: StateFlow<ImageBitmap?> = _bitmapFlow

    val scaleMode: SpectrogramScaleMode =
        settingsService.get(SettingsKey.SettingSpectrogramScaleMode)

    val yAxisTicks: List<String> = when (scaleMode) {
        SpectrogramScaleMode.SCALE_LOG -> frequencyLegendPositionLog
        SpectrogramScaleMode.SCALE_LINEAR -> frequencyLegendPositionLinear
    }.map { it.toFrequencyString() }


    // - Lifecycle

    init {
        viewModelScope.launch {
            // Listen to spectrum data updates and build spectrogram along the way
            liveAudioService.getSpectrogramDataFlow()
                .flowOn(Dispatchers.Default)
                .collect { spectrumData ->
                    pushToBitmap(getSpectrogramLine(spectrumData))
                }
        }
    }


    // - Public functions

    /**
     * Updates the canvas size used to generate spectrogram bitmaps.
     * Should be called when screen size changes.
     *
     * @param newSize New canvas size.
     */
    fun updateCanvasSize(newSize: IntSize, newDensity: Density) {
        if (newSize == canvasSize && newDensity == canvasDensity) {
            return
        }

        logger.debug("Updating spectrogram canvas size: [W: ${newSize.width}, H: ${newSize.height}]")

        canvasDensity = newDensity
        canvasSize = newSize
        currentBitmap = null
    }


    // - Private functions

    private fun getSpectrogramLine(
        spectrogramData: SpectrogramData,
    ): List<Color> {
        if (canvasSize == IntSize.Zero) {
            return emptyList()
        }

        // generate columns of pixels
        // merge power of each frequencies following the destination bitmap resolution
        val sampleRate = spectrogramData.sampleRate.toDouble()
        val hertzBySpectrumCell = sampleRate / FFT_SIZE.toDouble()
        val frequencyLegendPosition = when (scaleMode) {
            SpectrogramScaleMode.SCALE_LOG -> frequencyLegendPositionLog
            else -> frequencyLegendPositionLinear
        }
        var lastProcessFrequencyIndex = 0
        val freqByPixel = spectrogramData.spectrum.size / canvasSize.height.toDouble()

        return (0..<canvasSize.height).map { pixel ->
            var freqStart: Int
            var freqEnd: Int
            if (scaleMode == SpectrogramScaleMode.SCALE_LOG) {
                freqStart = lastProcessFrequencyIndex
                val fMax = sampleRate / 2
                val fMin = frequencyLegendPosition[0]
                val r = fMax / fMin.toDouble()
                val f = fMin * 10.0.pow(pixel * log10(r) / canvasSize.height)
                val nextFrequencyIndex =
                    min(spectrogramData.spectrum.size, (f / hertzBySpectrumCell).toInt())
                freqEnd =
                    min(spectrogramData.spectrum.size, (f / hertzBySpectrumCell).toInt() + 1)
                lastProcessFrequencyIndex = min(spectrogramData.spectrum.size, nextFrequencyIndex)
            } else {
                freqStart = floor(pixel * freqByPixel).toInt()
                freqEnd = min(
                    (pixel + 1) * freqByPixel,
                    spectrogramData.spectrum.size.toDouble()
                ).toInt()
            }
            var sumVal = 0.0
            for (idFreq in freqStart..<freqEnd) {
                sumVal += 10.0.pow(spectrogramData.spectrum[idFreq] / 10.0)
            }
            sumVal = max(0.0, 10 * log10(sumVal / (freqEnd - freqStart)) + DB_GAIN)

            SpectrogramColorRamp.getColorForValue(
                value = sumVal,
                min = MIN_DB,
                range = RANGE_DB,
            )
        }
    }

    private fun pushToBitmap(
        spectrogramLine: List<Color>,
    ) {
        if (canvasSize == IntSize.Zero) {
            return
        }

        val drawScope = CanvasDrawScope()
        val output = ImageBitmap(canvasSize.width, canvasSize.height)
        val canvas = Canvas(output)

        drawScope.draw(
            layoutDirection = LayoutDirection.Ltr,
            density = canvasDensity,
            canvas = canvas,
            size = canvasSize.toSize(),
        ) {

            // TODO: Calculate offset and scaling to match real time scale to plot x axis

            // Draw current spectrogram state offset by 1 pixel to the left.
            // Else, fill background with black.
            currentBitmap?.let {
                drawImage(it, topLeft = Offset(-1f, 0f))
            } ?: drawRect(
                color = SpectrogramColorRamp.palette.first(),
                size = size
            )

            // Draw new line at the right of the canvas
            spectrogramLine.reversed().forEachIndexed { index, color ->
                drawRect(
                    color,
                    topLeft = Offset(size.width - 1f, index.toFloat()),
                    size = Size(1f, 1f)
                )
            }
        }

        _bitmapFlow.tryEmit(output)
        currentBitmap = output
    }
}
