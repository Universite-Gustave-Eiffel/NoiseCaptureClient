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
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
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
import org.noiseplanet.noisecapture.ui.components.plot.AxisTick
import org.noiseplanet.noisecapture.ui.components.plot.PlotAxisSettings
import org.noiseplanet.noisecapture.ui.theme.SpectrogramColorRamp
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.toFrequencyString
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class SpectrogramPlotViewModel : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        private const val RANGE_DB = 100.0
        private const val MIN_DB = 0.0

        // TODO: Platform dependant gain?
        private const val DB_GAIN = AcousticIndicatorsProcessing.ANDROID_GAIN

        private val FRAME_RATE: Duration = (1.0 / 30.0).milliseconds

        // TODO: Make this a user settings property?
        val DISPLAYED_TIME_RANGE: Duration = 20.seconds
        val TICK_SPACING_TIME_RANGE: Duration = 5.seconds
        val X_TICKS_COUNT: Int = (DISPLAYED_TIME_RANGE / TICK_SPACING_TIME_RANGE).toInt()

        val Y_AXIS_TICKS_LOG = intArrayOf(
            63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000, 24000
        )
        val Y_AXIS_TICKS_LINEAR = IntArray(24) { it * 1000 + 1000 }
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val settingsService: UserSettingsService by inject()
    private val logger: Logger by injectLogger()

    private var canvasSize: IntSize = IntSize.Zero
    private var canvasDensity: Density = Density(1f)

    private var spectrogramUpdatesJob: Job? = null
    private var lastSpectrogramDataTimeStamp: Long? = null

    private var canvas: Canvas? = null
    private val drawScope: CanvasDrawScope = CanvasDrawScope()
    private var currentBitmap: ImageBitmap? = null

    private val _bitmapFlow: MutableStateFlow<SpectrogramBitmap?> = MutableStateFlow(null)
    val bitmapFlow: StateFlow<SpectrogramBitmap?> = _bitmapFlow

    val scaleMode: SpectrogramScaleMode =
        settingsService.get(SettingsKey.SettingSpectrogramScaleMode)

    val axisSettings = PlotAxisSettings(
        xTicks = (0..X_TICKS_COUNT)
            .map { index ->
                val tick = index * TICK_SPACING_TIME_RANGE.inWholeSeconds
                AxisTick(
                    value = tick.toDouble(),
                    label = "$tick s"
                )
            }.reversed(),
        yTicks = when (scaleMode) {
            SpectrogramScaleMode.SCALE_LOG -> Y_AXIS_TICKS_LOG
            SpectrogramScaleMode.SCALE_LINEAR -> Y_AXIS_TICKS_LINEAR
        }.map {
            AxisTick(
                value = it.toDouble(),
                label = it.toFrequencyString()
            )
        },
        yAxisLayoutDirection = LayoutDirection.Rtl
    )


    // - Public functions

    /**
     * Starts listening to incoming audio analysis data from live audio source and
     * push stripes to the spectrogram bitmap.
     */
    fun startSpectrogram() {
        if (spectrogramUpdatesJob != null) {
            // Spectrogram updates are already running
            return
        }

        // Create a flow that will emit a new timestamp value at FRAME_RATE rate.
        val fpsFlow = flow {
            while (currentCoroutineContext().isActive) {
                emit(Clock.System.now().toEpochMilliseconds())
                delay(FRAME_RATE)
            }
        }

        spectrogramUpdatesJob = viewModelScope.launch(Dispatchers.Default) {
            // Listen to spectrum data updates and build spectrogram along the way
            liveAudioService.getSpectrogramDataFlow()
                .map {
                    // For each new spectrogram data, build a pixels strip
                    getSpectrogramStrip(it)
                }
                .combine(fpsFlow) { stripPixels, timestamp ->
                    // Combine fixed FPS timer with new spectrogram strips updates
                    Pair(stripPixels, timestamp)
                }
                .collect { (stripPixels, timestamp) ->

                    // TODO: This could be further optimized by drawing every strip once and
                    //       calculating the width based on timestamp comparison, but then the
                    //       canvas size would change for every new spectrogram data and we would
                    //       to create a new bitmap everytime, increasing the memory impact.
                    //       For now the CPU cost tradeoff is acceptable.

                    // On every update, draw a new strip
                    pushToBitmap(
                        timestamp = timestamp,
                        stripPixels = stripPixels,
                    )
                }
        }
    }

    /**
     * Stops pushing new stripes to spectrogram bitmap.
     */
    fun stopSpectrogram() {
        spectrogramUpdatesJob?.cancel()
        spectrogramUpdatesJob = null
        currentBitmap = null
        lastSpectrogramDataTimeStamp = null
    }

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

    /**
     * Gets a spectrogram strip data consisting of colors to draw and where to draw them.
     *
     * @param spectrogramData Input data with noise level per frequency band.
     *
     * @return A map of colors associated to Y offsets of where to draw them along the vertical line.
     */
    private fun getSpectrogramStrip(
        spectrogramData: SpectrogramData,
    ): List<Color> {
        if (canvasSize == IntSize.Zero) {
            return emptyList()
        }

        // generate columns of pixels
        // merge power of each frequencies following the destination bitmap resolution
        val sampleRate = spectrogramData.sampleRate.toDouble()
        val hertzBySpectrumCell = sampleRate / FFT_SIZE.toDouble()
        var lastProcessFrequencyIndex = 0
        val freqByPixel = spectrogramData.spectrum.size / canvasSize.height.toDouble()

        val fMax = sampleRate / 2
        val fMin = Y_AXIS_TICKS_LOG[0]
        val r = fMax / fMin.toDouble()

        return (0..<canvasSize.height).map { pixel ->
            var freqStart: Int
            var freqEnd: Int
            if (scaleMode == SpectrogramScaleMode.SCALE_LOG) {
                freqStart = lastProcessFrequencyIndex
                val f = fMin * 10.0.pow(pixel * log10(r) / canvasSize.height)
                val index = (f / hertzBySpectrumCell).toInt()
                val nextFrequencyIndex = min(spectrogramData.spectrum.size, index)
                freqEnd = min(spectrogramData.spectrum.size, index + 1)
                lastProcessFrequencyIndex = nextFrequencyIndex
            } else {
                freqStart = (pixel * freqByPixel).toInt()
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

    /**
     * Draws the new spectrogram strip to the spectrogram canvas.
     */
    private fun pushToBitmap(
        timestamp: Long,
        stripPixels: List<Color>,
    ) {
        if (canvasSize == IntSize.Zero) return

        // If bitmap was invalidated, create a new one and update Canvas.
        if (currentBitmap == null) {
            initializeBitmap()
        }

        // Safely unwrap canvas and current bitmap for easier use
        val canvas = canvas ?: return
        val currentBitmap = currentBitmap ?: return

        // Calculate the width of the next spectrogram strip
        val pixelsPerMs = canvasSize.width.toFloat() /
            DISPLAYED_TIME_RANGE.inWholeMilliseconds.toFloat()
        val lastSpectrogramTimestamp =
            lastSpectrogramDataTimeStamp ?: (timestamp - ceil(1 / pixelsPerMs).toLong())
        val stripWidthFloat = (timestamp - lastSpectrogramTimestamp) * pixelsPerMs

        // If the new strip is less than one pixel large, drop it
        if (stripWidthFloat < 1) return

        // Round the strip width to an integer (drawing bitmap with floating point offset leads to
        // unexpected results), and save the skipped milliseconds for the next drawn strip
        val stripWidth = floor(stripWidthFloat)
        val pixelsReminder = stripWidthFloat - stripWidth
        val skippedMilliseconds = round(pixelsReminder / pixelsPerMs)
        lastSpectrogramDataTimeStamp = timestamp - skippedMilliseconds.toLong()

        drawScope.draw(
            layoutDirection = LayoutDirection.Ltr,
            density = canvasDensity,
            canvas = canvas,
            size = canvasSize.toSize(),
        ) {
            // Draw current spectrogram state offset to the left by the width of the next band
            drawImage(
                image = currentBitmap,
                topLeft = Offset(-stripWidth, 0f),
            )

            // Draw new line at the right of the canvas
            stripPixels.forEachIndexed { index, color ->
                drawRect(
                    color,
                    topLeft = Offset(size.width - stripWidth, (size.height - index)),
                    size = Size(stripWidth, 1f),
                )
            }
        }

        // Emit new bitmap data through state flow
        _bitmapFlow.tryEmit(SpectrogramBitmap(currentBitmap, timestamp))
    }

    /**
     * Creates a new image bitmap and fills it with background color
     */
    private fun initializeBitmap() {
        // Init ImageBitmap and Canvas
        ImageBitmap(canvasSize.width, canvasSize.height).apply {
            currentBitmap = this
            canvas = Canvas(this)
        }

        canvas?.let {
            // Fill the new bitmap with darkest color from palette
            drawScope.draw(
                layoutDirection = LayoutDirection.Ltr,
                density = canvasDensity,
                canvas = it,
                size = canvasSize.toSize(),
            ) {
                drawRect(
                    color = SpectrogramColorRamp.palette.first(),
                    size = size
                )
            }
        }
    }
}


/**
 * [ImageBitmap] wrapper with a timestamp property so that emitting a new value through our
 * state flow will effectively trigger a new composition because the class will have a different
 * hash, without having to copy the bitmap data to a new instance every time.
 */
data class SpectrogramBitmap(
    val bitmap: ImageBitmap,
    val timestamp: Long,
)
