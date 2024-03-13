package org.noise_planet.noisecapture.shared

import org.noise_planet.noisecapture.AudioSamples
import org.noise_planet.noisecapture.shared.child.FFT_HOP
import org.noise_planet.noisecapture.shared.child.FFT_SIZE
import org.noise_planet.noisecapture.shared.child.WINDOW_TIME
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.SpectrumData
import org.noise_planet.noisecapture.shared.signal.WindowAnalysis
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import kotlin.math.min
import kotlin.math.pow

class MeasurementService(val sampleRate: Int) {
    private var windowLength = (sampleRate * WINDOW_TIME).toInt()
    private var windowData = FloatArray(windowLength)
    private var windowDataCursor = 0
    private var windowAnalysis = WindowAnalysis(sampleRate, FFT_SIZE, FFT_HOP)
    private val spectrumChannel: SpectrumChannel = SpectrumChannel().apply {
        this.loadConfiguration(
            when (sampleRate) {
                48000 -> get48000HZ()
                else -> get44100HZ()
            }
        )
    }

    suspend fun processSamples(samples: AudioSamples): List<MeasurementServiceData> {
        val measurementServiceDataList = ArrayList<MeasurementServiceData>()
        val gain = (10.0.pow(105 / 20.0)).toFloat()
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
                val fftSpectrum = windowAnalysis.pushSamples(samples.epoch, windowData).toList()
                measurementServiceDataList.add(
                    MeasurementServiceData(
                        samples.epoch,
                        laeq,
                        fftSpectrum,
                        thirdOctave
                    )
                )
                windowDataCursor = 0
            }
        }
        return measurementServiceDataList
    }


}
data class MeasurementServiceData(val epoch : Long, val laeq: Double,
                                  val spectrumDataList : List<SpectrumData>,
                                  val thirdOctave : DoubleArray)

