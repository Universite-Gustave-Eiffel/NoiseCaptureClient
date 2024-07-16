package org.noiseplanet.noisecapture.shared.signal

import kotlin.math.log10

class DigitalFilter(var numerator: DoubleArray, var denominator: DoubleArray) {
    var order: Int
    var delay1: DoubleArray
    var delay2: FloatArray
    var circularIndex = 0

    init {
        order = numerator.size
        delay1 = DoubleArray(order)
        delay2 = FloatArray(order)
    }

    fun clearDelay() {
        circularIndex = 0
        delay1 = DoubleArray(order)
        delay2 = FloatArray(order)
    }

    /**
     * Direct form II transposed filter
     * @param samplesIn: Input samples
     * @link https://rosettacode.org/wiki/Apply_a_digital_filter_(direct_form_II_transposed)#Java
     * @param samplesIn
     * @param samplesOut
     */
    fun filter(samplesIn: FloatArray, samplesOut: FloatArray) {
        for (i in samplesIn.indices) {
            var inputAccumulator = 0.0
            delay2[circularIndex] = samplesIn[i]
            for (j in 0 until order) {
                var indexDelay2 = (circularIndex - j) % order
                if (indexDelay2 < 0) indexDelay2 += delay2.size
                inputAccumulator += numerator[j] * delay2[indexDelay2]
                if (j == 0) continue
                var indexDelay1 = (order - j + circularIndex) % order
                if (indexDelay1 < 0) {
                    indexDelay1 += delay1.size
                }
                inputAccumulator -= denominator[j] * delay1[indexDelay1]
            }
            inputAccumulator /= denominator[0]
            delay1[circularIndex] = inputAccumulator
            circularIndex++
            if (circularIndex == order) circularIndex = 0
            samplesOut[i] = inputAccumulator.toFloat()
        }
    }

    fun filterLeq(samplesIn: FloatArray): Double {
        var squareSum = 0.0
        for (i in samplesIn.indices) {
            var inputAccumulator = 0.0
            delay2[circularIndex] = samplesIn[i]
            for (j in 0 until order) {
                var indexDelay2 = (circularIndex - j) % order
                if (indexDelay2 < 0) indexDelay2 += delay2.size
                inputAccumulator += numerator[j] * delay2[indexDelay2]
                if (j == 0) continue
                var indexDelay1 = (order - j + circularIndex) % order
                if (indexDelay1 < 0) {
                    indexDelay1 += delay1.size
                }
                inputAccumulator -= denominator[j] * delay1[indexDelay1]
            }
            inputAccumulator /= denominator[0]
            delay1[circularIndex] = inputAccumulator
            circularIndex++
            if (circularIndex == order) circularIndex = 0
            squareSum += inputAccumulator * inputAccumulator
        }
        return 10 * log10(squareSum / samplesIn.size)
    }
}
