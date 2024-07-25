package org.noiseplanet.noisecapture.signal

import kotlinx.coroutines.test.runTest
import org.noiseplanet.noisecapture.audio.signal.Bluestein
import org.noiseplanet.noisecapture.audio.signal.SpectrumChannel
import org.noiseplanet.noisecapture.audio.signal.fft
import org.noiseplanet.noisecapture.audio.signal.fftFloat
import org.noiseplanet.noisecapture.audio.signal.get48000HZ
import org.noiseplanet.noisecapture.audio.signal.nextPowerOfTwo
import org.noiseplanet.noisecapture.audio.signal.realFFT
import org.noiseplanet.noisecapture.audio.signal.realFFTFloat
import org.noiseplanet.noisecapture.audio.signal.realIFFT
import org.noiseplanet.noisecapture.audio.signal.realIFFTFloat
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

class TestFFT {

    companion object {

        fun generateSinusoidalSignal(
            frequency: Double,
            sampleRate: Double,
            duration: Double,
            coefficient: (Int) -> Double = { 1.0 },
        ): DoubleArray {
            val numSamples = (duration * sampleRate).toInt()
            val signal = DoubleArray(numSamples, coefficient)

            val angularFrequency = 2.0 * PI * frequency / sampleRate

            for (i in 0 until numSamples) {
                signal[i] *= sin(i * angularFrequency)
            }

            return signal
        }

        fun generateSinusoidalFloatSignal(
            frequency: Double,
            sampleRate: Double,
            duration: Double,
            coefficient: (Int) -> Float = { 1.0f },
        ): FloatArray {
            val numSamples = (duration * sampleRate).toInt()
            val signal = FloatArray(numSamples, coefficient)

            val angularFrequency = 2.0 * PI * frequency / sampleRate

            for (i in 0 until numSamples) {
                signal[i] *= sin(i * angularFrequency).toFloat()
            }

            return signal
        }

        fun printNumpyArray(values: DoubleArray) {
            print("[")
            for (i in values.indices step 2) {
                if (i > 0) {
                    print(", ")
                }
                print("${values[i]}+${values[i + 1]}j")
            }
            println("]")
        }
    }


    @Test
    fun testFFTDouble() {
        val values = doubleArrayOf(
            0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 3.0, 0.0, 4.0, 0.0,
            5.0, 0.0, 6.0, 0.0, 7.0, 0.0
        )
        // np.hstack((result.real[:, None], result.imag[:, None])).flatten()
        val expected = doubleArrayOf(
            28.0, 0.0, -4.0, 9.65685424949238, -4.0, 4.0, -4.0,
            1.6568542494923806, -4.0, 0.0, -4.0, -1.6568542494923806, -4.0, -4.0, -4.0,
            -9.65685424949238
        )
        fft(values.size / 2, values)
        expected.forEachIndexed { index, value ->
            assertEquals(value, values[index], 1e-8)
        }
    }

    @Test
    fun testFFTFloat() {
        val values = floatArrayOf(
            0.0F, 0.0F, 1.0F, 0.0F, 2.0F, 0.0F, 3.0F, 0.0F, 4.0F, 0.0F,
            5.0F, 0.0F, 6.0F, 0.0F, 7.0F, 0.0F
        )
        // np.hstack((result.real[:, None], result.imag[:, None])).flatten()
        val expected = floatArrayOf(
            28.0F, 0.0F, -4.0F, 9.656855F, -4.0F, 4.0F, -4.0F,
            1.6568543F, -4.0F, 0.0F, -4.0F, -1.6568543F, -4.0F, -4.0F, -4.0F,
            -9.656855F
        )
        fftFloat(values.size / 2, values)
        expected.forEachIndexed { index, value ->
            assertEquals(value, values[index], 1e-6F)
        }
    }

    @Test
    fun testRFFT() {
        val values = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)
        // np.hstack((result.real[:, None], result.imag[:, None])).flatten()
        val expected = doubleArrayOf(
            28.0, 0.0, -4.0, 9.65685424949238, -4.0, 4.0, -4.0,
            1.6568542494923806, -4.0, 0.0
        )
        val rfftVal = realFFT(values)
        expected.forEachIndexed { index, value ->
            assertEquals(value, rfftVal[index], 1e-8)
        }
    }

    @Test
    fun testRFFT2() {
        val values = doubleArrayOf(
            0.00000000e+00, 8.31469612e-01, 9.23879533e-01, 1.95090322e-01,
            -7.07106781e-01, -9.80785280e-01, -3.82683432e-01, 5.55570233e-01,
            1.00000000e+00, 5.55570233e-01, -3.82683432e-01, -9.80785280e-01,
            -7.07106781e-01, 1.95090322e-01, 9.23879533e-01, 8.31469612e-01,
            6.12323400e-16, -8.31469612e-01, -9.23879533e-01, -1.95090322e-01,
            7.07106781e-01, 9.80785280e-01, 3.82683432e-01, -5.55570233e-01,
            -1.00000000e+00, -5.55570233e-01, 3.82683432e-01, 9.80785280e-01,
            7.07106781e-01, -1.95090322e-01, -9.23879533e-01, -8.31469612e-01
        )
        // list(np.hstack((result.real[:, None], result.imag[:, None])).flatten())
        val expected = doubleArrayOf(
            6.357727552008862e-15,
            0.0,
            -2.887112575184895e-15,
            7.586883716628833e-15,
            -4.681264813764543e-15,
            -2.3033681535895184e-15,
            1.168027486177751e-15,
            -5.27151586969653e-15,
            2.7067429112899688e-15,
            9.688948784381873e-16,
            -5.608327010386881e-15,
            -16.0,
            2.3949282893188493e-15,
            -1.876232130942726e-15,
            3.3851329943375557e-15,
            2.990967016227153e-15,
            -2.2187453132204726e-15,
            1.8596235662471372e-15,
            -1.0570661146844079e-15,
            -2.5601481068986296e-15,
            3.3044820170772633e-15,
            -3.2528972154800725e-17,
            -3.8319701709866305e-15,
            5.329070518200751e-15,
            -2.5923191367677723e-15,
            -2.0287072880497354e-15,
            -1.2824512606999475e-15,
            -7.0478727090967805e-15,
            1.4311481056631361e-15,
            -1.791932624351781e-15,
            5.215179454838043e-15,
            -5.069658764097952e-15,
            2.749502721977103e-15,
            0.0
        )
        val rfftVal = realFFT(values)
        expected.forEachIndexed { index, value ->
            assertEquals(value, rfftVal[index], 1e-8)
        }
    }

    @Test
    fun testRFFTSinus() {
        val frequency = 8.0 // Hz
        val sampleRate = 64.0 // Hz
        val duration = 2.0.pow(ceil(log2(sampleRate))) / sampleRate
        val samples = generateSinusoidalSignal(frequency, sampleRate, duration)
        val spectrum = realFFT(samples)
        val result = realIFFT(spectrum)
        assertEquals(samples.size, result.size)
        samples.forEachIndexed { index, value ->
            assertEquals(value, result[index], 1e-8)
        }
    }

    @Test
    fun testRFFTIncremental() {
        val samples = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val expected =
            doubleArrayOf(36.0, 0.0, -4.0, 9.65685425, -4.0, 4.0, -4.0, 1.65685425, -4.0, 0.0)
        val result = realFFT(samples)
        assertEquals(expected.size, result.size)
        expected.forEachIndexed { index, value ->
            assertEquals(value, result[index], 1e-8)
        }
        val origin = realIFFT(result)
        assertEquals(samples.size, origin.size)
        samples.forEachIndexed { index, value ->
            assertEquals(value, origin[index], 1e-8)
        }
    }


    @Test
    fun testRFFTFloatSinus() {
        val frequency = 8.0 // Hz
        val sampleRate = 64.0 // Hz
        val duration = 2.0.pow(ceil(log2(sampleRate))) / sampleRate
        val samples = generateSinusoidalFloatSignal(frequency, sampleRate, duration)
        val spectrum = realFFTFloat(samples)
        val result = realIFFTFloat(spectrum)
        assertEquals(samples.size, result.size)
        samples.forEachIndexed { index, value ->
            assertEquals(value, result[index], 1e-7f)
        }
    }

    @Test
    fun testRFFTFloatIncremental() {
        val samples = floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f)
        val expected =
            floatArrayOf(
                36.0f, 0.0f, -4.0f, 9.656855f, -4.0f, 4.0f, -4.0f,
                1.6568543f, -4.0f, 0.0f
            )
        val result = realFFTFloat(samples)
        assertEquals(expected.size, result.size)
        expected.forEachIndexed { index, value ->
            assertEquals(value, result[index], 1e-5f)
        }
        val origin = realIFFTFloat(result)
        assertEquals(samples.size, origin.size)
        samples.forEachIndexed { index, value ->
            assertEquals(value, origin[index], 1e-5f)
        }
    }

    @Test
    fun testRFFTSinusLogScale() = runTest {
        val sampleRate = 64
        val expectedLevel = 94.0
        val peak = 10.0.pow(expectedLevel / 20.0) * sqrt(2.0)
        val sum: (Double, Double) -> Double = { x: Double, y: Double -> x + y }
        val frequencyPeaks = doubleArrayOf(5.0, 12.0, 20.0, 28.0)
        var signal = DoubleArray(sampleRate)
        // sum multiple sinusoidal signals
        frequencyPeaks.forEach { frequencyPeak ->
            signal = signal.zip(
                generateSinusoidalSignal(frequencyPeak,
                    sampleRate.toDouble(), 1.0, coefficient = { peak }), sum
            ).toDoubleArray()
        }

        val fr = realFFT(signal)
        val magnitudeSquared =
            DoubleArray(fr.size / 2) { i: Int -> fr[(i * 2) + 1] * fr[(i * 2) + 1] }
        val vref = (signal.size * signal.size) / 2
        val levels = magnitudeSquared.map { 10 * log10(it / vref) }
        frequencyPeaks.forEach {
            assertEquals(expectedLevel, levels[it.toInt()], 1e-8)
        }
    }

    @Test
    fun testBluestein() {
        val input = doubleArrayOf(
            -6.0, 0.0, -5.0, 0.0, -4.0, 0.0, -3.0, 0.0, -2.0, 0.0, -1.0, 0.0,
            0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 3.0, 0.0, 4.0, 0.0, 5.0, 0.0
        )
        val bluestein = Bluestein(input.size / 2)
        val expectedChirp = doubleArrayOf(
            0.965925826289071,
            -0.2588190451025107,
            0.5000000000000081,
            -0.8660254037844339,
            -0.7071067811865438,
            -0.7071067811865512,
            -0.5000000000000047,
            0.8660254037844359,
            0.9659258262890694,
            -0.2588190451025166,
            -1.0,
            -3.920107718544707E-15,
            0.9659258262890689,
            -0.2588190451025185,
            -0.5000000000000012,
            0.8660254037844379,
            -0.7071067811865468,
            -0.7071067811865482,
            0.5000000000000003,
            -0.8660254037844385,
            0.9659258262890683,
            -0.2588190451025207,
            1.0,
            -0.0,
            0.9659258262890683,
            -0.2588190451025207,
            0.5000000000000003,
            -0.8660254037844385,
            -0.7071067811865468,
            -0.7071067811865482,
            -0.5000000000000012,
            0.8660254037844379,
            0.9659258262890689,
            -0.2588190451025185,
            -1.0,
            -3.920107718544707E-15,
            0.9659258262890694,
            -0.2588190451025166,
            -0.5000000000000047,
            0.8660254037844359,
            -0.7071067811865438,
            -0.7071067811865512,
            0.5000000000000081,
            -0.8660254037844339,
            0.965925826289071,
            -0.2588190451025107
        )
        assertEquals(expectedChirp.size, bluestein.chirp.size)
        expectedChirp.forEachIndexed { index, expected ->
            assertEquals(expected, bluestein.chirp[index], 1e-8)
        }
        val expectedFft = doubleArrayOf(
            -6.0, 0.0, -6.0, 22.39230485, -6.0, 10.39230485, -6.0, 6.0, -6.0, 3.46410162,
            -6.0, 1.60769515, -6.0, -0.0, -6.0, -1.60769515, -6.0, -3.46410162, -6.0, -6.0,
            -6.0, -10.39230485, -6.0, -22.39230485
        )
        val got = bluestein.fft(input)
        assertEquals(expectedFft.size, got.size)
        expectedFft.forEachIndexed { index, expected ->
            assertEquals(expected, got[index], 1e-8, message = "Index $index")
        }
    }

    @Test
    @Ignore // not a unit test
    fun benchBluestein() = runTest {
        val input = DoubleArray(60000 * 2) { cos(it.toDouble()) }
        val bluestein = Bluestein(input.size / 2)
        val timings = LongArray(64) {
            measureTime {
                bluestein.fft(input)
            }.inWholeMilliseconds
        }
        val timings2 = LongArray(64) {
            measureTime {
                val dim = nextPowerOfTwo(input.size)
                fft(dim / 2, DoubleArray(dim))
            }.inWholeMilliseconds
        }
        val sc = SpectrumChannel()
        sc.loadConfiguration(get48000HZ(), true)
        val timings3 = LongArray(64) {
            measureTime {
                sc.processSamples(FloatArray(input.size / 2), false).sum()
            }.inWholeMilliseconds
        }
        println(
            "min ${timings.min()} max ${timings.max()}" +
                " median ${
                    timings.sorted()[if (timings.size % 2 == 0) timings.size / 2 - 1 else timings.size / 2]
                }"
        )
        println(
            "min ${timings2.min()} max ${timings2.max()}" +
                " median ${
                    timings2.sorted()[if (timings2.size % 2 == 0) timings2.size / 2 - 1 else timings2.size / 2]
                }"
        )
        println(
            "min ${timings3.min()} max ${timings3.max()}" +
                " median ${
                    timings3.sorted()[if (timings3.size % 2 == 0) timings3.size / 2 - 1 else timings3.size / 2]
                }"
        )
    }
}
