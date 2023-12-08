package org.noise_planet.noisecapture.shared.signal

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFFT {
    private fun generateSinusoidalSignal(
        frequency: Double,
        sampleRate: Double,
        duration: Double
    ): DoubleArray {
        val numSamples = (duration * sampleRate).toInt()
        val signal = DoubleArray(numSamples)

        val angularFrequency = 2.0 * PI * frequency / sampleRate

        for (i in 0 until numSamples) {
            signal[i] = sin(i * angularFrequency)
        }

        return signal
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
}
