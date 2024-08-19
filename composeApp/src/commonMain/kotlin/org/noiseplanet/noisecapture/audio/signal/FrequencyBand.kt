package org.noiseplanet.noisecapture.audio.signal

import kotlin.math.pow

data class FrequencyBand(
    val minFrequency: Double,
    val midFrequency: Double,
    val maxFrequency: Double,
    var spl: Double,
) {

    enum class BaseMethod {
        B10,
        B2
    }

    companion object {

        /**
         * Create (third-)octave array from the specified parameters (without spl values)
         *
         * @param firstFrequencyBand First frequency band (Hz)
         * @param lastFrequencyBand Last frequency band (Hz)
         * @param base Octave base 2 or 10
         * @param bandDivision Octave bands division (defaults to 3 for third octaves)
         */
        fun emptyFrequencyBands(
            firstFrequencyBand: Double,
            lastFrequencyBand: Double,
            base: BaseMethod = BaseMethod.B10,
            bandDivision: Double = 3.0,
        ): Array<FrequencyBand> {
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
    }
}
