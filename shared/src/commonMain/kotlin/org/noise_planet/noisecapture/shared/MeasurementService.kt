package org.noise_planet.noisecapture.shared

import kotlinx.atomicfu.atomic

class MeasurementService {
    private val canceled = atomic(false)
    private val storageActivated = atomic(false)


    fun startRecording() {
        canceled.value = false

    }

}
