package org.noise_planet.noisecapture.shared

import org.noise_planet.noisecapture.AudioSamples
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import kotlin.math.min
import kotlin.math.pow

const val WINDOW_TIME = 0.125

class AcousticIndicatorsProcessing(val sampleRate: Int, dbGain : Double) {
    private var windowLength = (sampleRate * WINDOW_TIME).toInt()
    private var windowData = FloatArray(windowLength)
    private var windowDataCursor = 0
    val gain = (10.0.pow(dbGain / 20.0)).toFloat()

    private val spectrumChannel: SpectrumChannel = SpectrumChannel().apply {
        this.loadConfiguration(
            when (sampleRate) {
                48000 -> get48000HZ()
                else -> get44100HZ()
            }
        )
    }



    fun processSamples(samples: AudioSamples): List<AcousticIndicatorsData> {
        val acousticIndicatorsDataList = ArrayList<AcousticIndicatorsData>()
        var samplesProcessed = 0
        while (samplesProcessed < samples.samples.size) {
            while (windowDataCursor < windowLength &&
                samplesProcessed < samples.samples.size
            ) {
                val remainingToProcess = min(
                    windowLength - windowDataCursor,
                    samples.samples.size - samplesProcessed
                )
                for (i in 0..<remainingToProcess) {
                    windowData[i + windowDataCursor] =
                        samples.samples[i + samplesProcessed] * gain
                }
                windowDataCursor += remainingToProcess
                samplesProcessed += remainingToProcess
            }
            if (windowDataCursor == windowLength) {
                // window complete
                val laeq = spectrumChannel.processSamplesWeightA(windowData)
                val thirdOctave = DoubleArray(0)
                //val thirdOctave = spectrumChannel.processSamples(windowData)
                acousticIndicatorsDataList.add(
                    AcousticIndicatorsData(
                        samples.epoch,
                        laeq,
                        thirdOctave
                    )
                )
                windowDataCursor = 0
            }
        }
        return acousticIndicatorsDataList
    }
}

data class AcousticIndicatorsData(val epoch : Long, val laeq: Double, val thirdOctave : DoubleArray)

