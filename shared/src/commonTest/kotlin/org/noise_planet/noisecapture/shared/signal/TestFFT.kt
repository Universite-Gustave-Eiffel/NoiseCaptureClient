package org.noise_planet.noisecapture.shared.signal

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals


fun generateSinusoidalSignal(
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

fun generateSinusoidalFloatSignal(
    frequency: Double,
    sampleRate: Double,
    duration: Double,
    coefficient: (Int) -> Float = {1.0f}
): FloatArray {
    val numSamples = (duration * sampleRate).toInt()
    val signal = FloatArray(numSamples, coefficient)

    val angularFrequency = 2.0 * PI * frequency / sampleRate

    for (i in 0 until numSamples) {
        signal[i] *= sin(i * angularFrequency).toFloat()
    }

    return signal
}

class TestFFT {

    @Test
    fun testFFT() {
        val values = doubleArrayOf(0.0,0.0, 1.0,0.0, 2.0,0.0, 3.0,0.0, 4.0,0.0, 5.0,0.0, 6.0,0.0, 7.0,0.0)
        fft(values.size/2, values)
        print(values.joinToString(","))
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
            floatArrayOf(36.0f, 0.0f, -4.0f, 9.656855f, -4.0f, 4.0f, -4.0f,
                1.6568543f, -4.0f, 0.0f)
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
}
