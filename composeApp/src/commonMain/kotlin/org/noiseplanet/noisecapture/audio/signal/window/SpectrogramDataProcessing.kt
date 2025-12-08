package org.noiseplanet.noisecapture.audio.signal.window

import org.noiseplanet.noisecapture.audio.signal.bluestein.BluesteinFloat
import org.noiseplanet.noisecapture.audio.signal.fft.nextPowerOfTwo
import org.noiseplanet.noisecapture.audio.signal.fft.realFFT
import org.noiseplanet.noisecapture.audio.signal.fft.realFFTFloat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.min

/**
 * Computation of STFT (Short Time Fourier Transform)
 *
 * @param sampleRate Sample rate to compute epoch
 * @param windowSize Size of the window
 * @param windowHop Run a new analysis each windowHop samples
 */
class SpectrogramDataProcessing(
    val sampleRate: Int,
    val windowSize: Int,
    private val windowHop: Int,
    applyHannWindow: Boolean = true,
) {

    private val circularSamplesBuffer: FloatArray = FloatArray(windowSize)
    private var circularBufferCursor: Int = 0
    private val bluestein: BluesteinFloat? =
        if (nextPowerOfTwo(windowSize) != windowSize) {
            BluesteinFloat(windowSize)
        } else {
            null
        }

    // Windowing correction  factors
    // [1] F. J. Harris, “On the use of windows for harmonic analysis with the discrete fourier
    // transform,”Proceedings of the IEEE, vol. 66, no. 1, pp. 51–83, Jan. 1978.
    private val windowCorrectionFactor: Double =
        if (applyHannWindow) {
            0.375
        } else {
            1.0
        }
    private val vRef = (((windowSize * windowSize) / 2.0) * windowCorrectionFactor).toFloat()

    var samplesUntilWindow: Int = windowSize
    val hannWindow: FloatArray? =
        if (applyHannWindow) {
            FloatArray(windowSize) {
                (0.5 * (1 - cos(2 * PI * it / (windowSize - 1)))).toFloat()
            }
        } else {
            null
        }


    init {
        require(windowHop > 0) {
            "Window hop must be greater than 0"
        }
    }

    /**
     * Process the provided samples and run a STFFT analysis when a window is complete
     */
    fun pushSamples(
        epoch: Long,
        samples: FloatArray,
        processedWindows: MutableList<Window>? = null,
    ): Sequence<SpectrogramData> = sequence {
        var processed = 0
        while (processed < samples.size) {
            var toFetch = min(samples.size - processed, samplesUntilWindow)
            // fill the circular buffer
            while (toFetch > 0) {
                val copySize = min(circularSamplesBuffer.size - circularBufferCursor, toFetch)
                samples.copyInto(
                    circularSamplesBuffer,
                    circularBufferCursor,
                    processed,
                    processed + copySize
                )
                circularBufferCursor += copySize
                processed += copySize
                toFetch -= copySize
                samplesUntilWindow -= copySize
                if (circularBufferCursor == circularSamplesBuffer.size) {
                    circularBufferCursor = 0
                }
            }
            if (samplesUntilWindow == 0) {
                // window complete push it
                val windowSamples = FloatArray(windowSize)
                circularSamplesBuffer.copyInto(
                    windowSamples,
                    windowSize - circularBufferCursor,
                    0,
                    circularBufferCursor
                )
                circularSamplesBuffer.copyInto(
                    windowSamples,
                    0,
                    circularBufferCursor,
                    circularSamplesBuffer.size
                )
                // apply window function
                if (hannWindow != null) {
                    for (i in windowSamples.indices) {
                        windowSamples[i] *= hannWindow[i]
                    }
                }
                val window = Window(
                    (epoch - ((samples.size - processed) / sampleRate.toDouble()) * 1000.0).toLong(),
                    windowSamples
                )
                yield(processWindow(window))
                processedWindows?.add(window)
                samplesUntilWindow = windowHop
            }
        }
    }

    /**
     * TODO: Move to an extension in test files since it is not used by app code.
     */
    fun reconstructOriginalSignal(processedWindows: List<Window>): FloatArray {
        val sum = FloatArray(processedWindows.size + processedWindows.size * windowHop)
        for (i in processedWindows.indices) {
            for (j in 0..<windowSize) {
                val to = i * windowHop + j
                if (to < sum.size) {
                    sum[to] += processedWindows[i].samples[j]
                }
            }
        }
        return sum
    }

    /**
     * @see <a href="https://www.dsprelated.com/freebooks/sasp/Filling_FFT_Input_Buffer.html">Filling the FFT Input Buffer</a>
     */
    private fun processWindow(window: Window): SpectrogramData {
        return SpectrogramData(window.epoch, processWindowFloat(window), sampleRate)
    }

    private fun processWindowFloat(window: Window): FloatArray {
        require(window.samples.size == windowSize)
        val fr = (bluestein?.fft(window.samples) ?: realFFTFloat(window.samples))
        return FloatArray(fr.size / 2) { i: Int ->
            10 * log10((fr[(i * 2) + 1] * fr[(i * 2) + 1]) / vRef)
        }
    }

    @Suppress("UnusedPrivateMember") // Unused for now but might come in handy later
    private fun processWindowDouble(window: Window): DoubleArray {
        val fftWindowSize = nextPowerOfTwo(windowSize)
        val fftWindow = DoubleArray(fftWindowSize)
        val startIndex = windowSize / 2
        for (i in startIndex..<windowSize) {
            fftWindow[i - startIndex] = window.samples[i].toDouble()
        }
        val destinationOffset = fftWindowSize - (windowSize / 2)
        for (i in 0..<startIndex) {
            fftWindow[i + destinationOffset] = window.samples[i].toDouble()
        }
        return realFFT(fftWindow)
    }
}

data class SpectrogramData(val epoch: Long, val spectrum: FloatArray, val sampleRate: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SpectrogramData

        if (epoch != other.epoch) return false
        if (!spectrum.contentEquals(other.spectrum)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = epoch.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        return result
    }
}
