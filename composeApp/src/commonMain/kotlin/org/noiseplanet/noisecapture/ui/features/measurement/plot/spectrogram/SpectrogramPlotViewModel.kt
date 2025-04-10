package org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.ANDROID_GAIN
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.enums.SpectrogramScaleMode
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.util.injectLogger

class SpectrogramPlotViewModel : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        private const val RANGE_DB = 40.0
        private const val MIN_DB = 0.0
        private const val DB_GAIN = ANDROID_GAIN // TODO: Platform dependant gain?

        const val REFERENCE_LEGEND_TEXT = " +99s "
        const val SPECTROGRAM_STRIP_WIDTH = 32
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val logger: Logger by injectLogger()

    private var canvasSize: IntSize = IntSize.Zero
    private val spectrogramBitmaps = mutableStateListOf<SpectrogramBitmap>()

    val scaleMode = SpectrogramScaleMode.SCALE_LOG

    val sampleRateFlow: Flow<Double> = liveAudioService
        .getSpectrumDataFlow()
        .map { it.sampleRate.toDouble() }

    val currentStripData: SpectrogramBitmap?
        get() = spectrogramBitmaps.lastOrNull()

    val spectrogramBitmapFlow: StateFlow<List<SpectrogramBitmap>>
        get() = MutableStateFlow(spectrogramBitmaps)


    // - Lifecycle

    init {
        viewModelScope.launch {
            // Listen to spectrum data updates and build spectrogram along the way
            liveAudioService.getSpectrumDataFlow()
                .collect { spectrumData ->
                    currentStripData?.let { currentStripData ->
                        // Update current strip data
                        val newStripData = currentStripData.copy()
                        newStripData.pushSpectrumData(
                            spectrumData, MIN_DB, RANGE_DB, DB_GAIN
                        )
                        spectrogramBitmaps[spectrogramBitmaps.size - 1] = newStripData

                        if (currentStripData.offset == SPECTROGRAM_STRIP_WIDTH) {
                            // Spectrogram band complete, push new band to list
                            if ((spectrogramBitmaps.size - 1) * SPECTROGRAM_STRIP_WIDTH > canvasSize.width) {
                                // remove offscreen bitmaps
                                spectrogramBitmaps.removeAt(0)
                            }
                            withContext(Dispatchers.Main) {
                                spectrogramBitmaps.add(
                                    SpectrogramBitmap(
                                        size = IntSize(
                                            width = SPECTROGRAM_STRIP_WIDTH,
                                            height = canvasSize.height,
                                        ),
                                        scaleMode = scaleMode,
                                    )
                                )
                            }
                        }
                    }
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
    fun updateCanvasSize(newSize: IntSize) {
        if (newSize == canvasSize) {
            return
        }

        logger.debug("Updating spectrogram canvas size: [W: ${newSize.width}, H: ${newSize.height}]")

        canvasSize = newSize
        spectrogramBitmaps.clear()
        spectrogramBitmaps.add(
            SpectrogramBitmap(
                size = canvasSize,
                scaleMode = scaleMode,
            )
        )
    }
}
