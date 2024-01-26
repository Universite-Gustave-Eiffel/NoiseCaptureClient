package org.noise_planet.noisecapture.shared.signal

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.koin.core.time.measureDuration


class SpectrumChannelTest {

    @Test
    fun test1000HZ() = runTest {
        val sampleRate = 48000
        val expectedLevel = 94
        val peak = 10.0.pow(expectedLevel/20.0)* sqrt(2.0)
        val sum: (Float, Float) -> Float = { x: Float, y: Float -> x + y }
        var signal = generateSinusoidalFloatSignal(1000.0, sampleRate.toDouble(), 0.5)
            .zip(generateSinusoidalFloatSignal(1600.0, sampleRate.toDouble(), 1.0), sum)
            .toFloatArray().zip(
                generateSinusoidalFloatSignal(4000.0, sampleRate.toDouble(), 1.0), sum
            ).toFloatArray().zip(
                generateSinusoidalFloatSignal(125.0, sampleRate.toDouble(), 1.0), sum
            ).toFloatArray()
        signal = signal.map(fun(it: Float): Float {
            return (peak * it).toFloat()
        }).toFloatArray()


        val sc = SpectrumChannel()
        sc.loadConfiguration(get48000HZ(), true)

        if(signal.size % sc.minimumSamplesLength > 0) {
            // pad with zero
            signal = signal.copyOf(signal.size + (signal.size % sc.minimumSamplesLength))
        }

        val time = measureDuration {

            val toctaves = sc.processSamples(signal)
            println((sc.getNominalFrequency() zip toctaves.toList()))
        }

        println("done in $time ms")
    }
}