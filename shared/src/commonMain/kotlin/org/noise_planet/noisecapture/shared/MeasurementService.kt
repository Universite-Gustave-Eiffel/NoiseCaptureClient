package org.noise_planet.noisecapture.shared

import kotlinx.atomicfu.atomic
import kotlin.properties.Delegates

typealias AcousticIndicatorsCallback = (acousticIndicatorsData: AcousticIndicatorsData) -> Unit

class MeasurementService {
    private val canceled = atomic(false)
    private val storageActivated = atomic(false)
    var acousticIndicatorsObservers = mutableListOf<AcousticIndicatorsCallback>()
    var lastAcousticIndicator: AcousticIndicatorsData by Delegates.observable(
        AcousticIndicatorsData(0, 0.0, doubleArrayOf())
    ) { property, oldValue, newValue ->
        acousticIndicatorsObservers.forEach { it(newValue) }
    }

    /**
     * Start collecting measurements to be forwarded to observers
     */
    fun startRecording() {
        canceled.value = false

    }

    /**
     * Store measurement in database
     */
    fun startStorage() {

    }

}
