package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

/**
 * Calibration of device using road traffic.
 * Using average noise emission law of vehicles it can estimate the microphone sensitivity
 * from the following data
 */
@Serializable
data class TrafficCalibrationSession(
    val measurementUTC: Long,
    val microphoneIdentifier: String, // material unique identifier used to record the noise level
    val medianPeak: Double, // Median peak decibel value (without gain or any processing)
    val trafficCount: Int, // number of peaks
    val estimatedDistance: Double, // distance between microphone and road center
    val estimatedSpeed: Double // average estimated speed of vehicles
)
