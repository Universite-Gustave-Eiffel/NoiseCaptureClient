package org.noiseplanet.noisecapture.shared.signal

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Computation of STFT (Short Time Fourier Transform)
 * @sampleRate Sample rate to compute epoch
 * @windowSize Size of the window
 * @windowHop Run a new analysis each windowHop samples
 */
class WindowAnalysis(val sampleRate : Int, val windowSize : Int, val windowHop : Int, private val applyHannWindow  : Boolean = true) {
    val circularSamplesBuffer = FloatArray(windowSize)
    var circularBufferCursor = 0
    var samplesUntilWindow = windowSize
    val bluestein = if(nextPowerOfTwo(windowSize)!=windowSize) BluesteinFloat(windowSize) else null
    val hannWindow: FloatArray? = when (applyHannWindow) {
        true ->
            FloatArray(windowSize) {
                (0.5 * (1 - cos(2 * PI * it / (windowSize - 1)))).toFloat()
            }
        else -> null
    }
    //Windowing correction  factors
    // [1] F. J. Harris, “On the use of windows for harmonic analysis with the discrete fourier
    // transform,”Proceedings of the IEEE, vol. 66, no. 1, pp. 51–83, Jan. 1978.
    val windowCorrectionFactor = when (applyHannWindow) {
        true -> 0.375
        else -> 1.0
    }

    init {
        require(windowHop > 0) {
            "Window hop must be superior than 0"
        }
    }

    /**
     * Process the provided samples and run a STFFT analysis when a window is complete
     */
    fun pushSamples(
        epoch: Long,
        samples: FloatArray,
        processedWindows: MutableList<Window>? = null,
    ): Sequence<SpectrumData> = sequence {
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
    private fun processWindow(window: Window): SpectrumData {
        return SpectrumData(window.epoch, processWindowFloat(window), sampleRate)
    }

    fun processWindowFloat(window: Window) : FloatArray {
        require(window.samples.size == windowSize)
        val fr = (bluestein?.fft(window.samples) ?: realFFTFloat(window.samples))
        val vRef = (((windowSize*windowSize)/2.0)*windowCorrectionFactor).toFloat()
        return FloatArray(fr.size / 2) { i: Int -> 10 * log10((fr[(i*2)+1]*fr[(i*2)+1]) /vRef) }
    }

    fun processWindowDouble(window: Window): DoubleArray {
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

data class Window(val epoch: Long, val samples: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Window

        if (epoch != other.epoch) return false
        if (!samples.contentEquals(other.samples)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = epoch.hashCode()
        result = 31 * result + samples.contentHashCode()
        return result
    }
}

data class SpectrumData(val epoch: Long, val spectrum: FloatArray, val sampleRate: Int) {

    enum class BaseMethod {
        B10,
        B2
    }

    enum class OctaveWindow {
        RECTANGULAR,
        FRACTIONAL
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SpectrumData

        if (epoch != other.epoch) return false
        if (!spectrum.contentEquals(other.spectrum)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = epoch.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        return result
    }

    companion object {

        private fun getBands(
            bandIndex: Int,
            g: Double,
            bandDivision: Double,
        ): Triple<Double, Double, Double> {
            val fMid = g.pow(bandIndex / bandDivision) * 1000.0
            val fMax = g.pow(1.0 / (2.0 * bandDivision)) * fMid
            val fMin = g.pow(-1.0 / (2.0 * bandDivision)) * fMid
            return Triple(fMin, fMid, fMax)
        }

        private fun getBandIndexByFrequency(
            targetFrequency: Double,
            g: Double,
            bandDivision: Double,
        ): Int {
            var frequencyBandIndex = 0
            var (fMin, fMid, fMax) = getBands(frequencyBandIndex, g, bandDivision)
            while (!(fMin < targetFrequency && targetFrequency < fMax)) {
                if (targetFrequency < fMin) {
                    frequencyBandIndex -= 1
                } else if (targetFrequency > fMax) {
                    frequencyBandIndex += 1
                }
                val bandInfo = getBands(frequencyBandIndex, g, bandDivision)
                fMin = bandInfo.first
                fMax = bandInfo.third
            }
            return frequencyBandIndex
        }

        /**
         * Create (third-)octave array from the specified parameters (without spl values)
         */
        fun emptyFrequencyBands(firstFrequencyBand : Double,
                                lastFrequencyBand : Double, base : BaseMethod = BaseMethod.B10,
                                bandDivision : Double = 3.0) : Array<FrequencyBand> {
            val g = when (base) {
                BaseMethod.B10 -> 10.0.pow(3.0 / 10.0)
                BaseMethod.B2 -> 2.0
            }
            val firstBandIndex = getBandIndexByFrequency(firstFrequencyBand, g, bandDivision)
            val lastBandIndex = getBandIndexByFrequency(lastFrequencyBand, g, bandDivision)
            return Array(lastBandIndex - firstBandIndex) { bandIndex ->
                val (fMin, fMid, fMax) = getBands(bandIndex + firstBandIndex, g, bandDivision)
                FrequencyBand(fMin, fMid, fMax, 0.0)
            }
        }
    }

    /**
     * @see <a href="https://www.ap.com/technical-library/deriving-fractional-octave-spectra-from-the-fft-with-apx/">ref</a>
     * Class 0 filter is 0.15 dB error according to IEC 61260
     * @sampleRate sample rate
     * @firstFrequencyBand Skip bands up to specified frequency
     * @lastFrequencyBand Skip bands higher than this frequency
     * @base Octave base 10 or base 2
     * @octaveWindow Rectangular association of frequency band or fractional close to done by a filter
     */
    @Suppress("NestedBlockDepth")
    fun thirdOctaveProcessing(
        firstFrequencyBand: Double,
        lastFrequencyBand: Double,
        base: BaseMethod = BaseMethod.B10,
        bandDivision: Double = 3.0,
        octaveWindow: OctaveWindow = OctaveWindow.FRACTIONAL,
    ): Array<FrequencyBand> {
        val freqByCell: Double = (spectrum.size.toDouble() * 2) / sampleRate
        val thirdOctave =
            emptyFrequencyBands(firstFrequencyBand, lastFrequencyBand, base, bandDivision)

        if (octaveWindow == OctaveWindow.FRACTIONAL) {
            for (band in thirdOctave) {
                for (cellIndex in spectrum.indices) {
                    val f = (cellIndex + 1) / freqByCell
                    val division =
                        (f / band.midFrequency - band.midFrequency / f) * 1.507 * bandDivision
                    val cellGain = sqrt(1.0 / (1.0 + division.pow(6)))
                    val fg = 10.0.pow(spectrum[cellIndex] / 10.0) * cellGain
                    if (fg.isFinite()) {
                        band.spl += fg
                    }
                }
            }
            for (band in thirdOctave) {
                band.spl = 10 * log10(band.spl)
            }
        } else {
            for (band in thirdOctave) {
                val minCell = max(0, floor(band.minFrequency * freqByCell).toInt())
                val maxCell = min(spectrum.size, ceil(band.maxFrequency * freqByCell).toInt())
                var rms = 0.0
                for (cellIndex in minCell..<maxCell) {
                    val fg = 10.0.pow(spectrum[cellIndex] / 10.0)
                    if (fg.isFinite()) {
                        rms += fg
                    }
                }
                band.spl = 10 * log10(rms)
            }
        }
        return thirdOctave
    }
}

data class FrequencyBand(
    val minFrequency: Double,
    val midFrequency: Double,
    val maxFrequency: Double,
    var spl: Double,
)
