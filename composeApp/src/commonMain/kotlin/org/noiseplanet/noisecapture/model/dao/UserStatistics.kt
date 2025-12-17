package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * Current model version, used for potential migrations.
 */
val UserStatistics.Companion.VERSION: Int get() = 1

/**
 * Keeps track of current user's measurement statistics.
 *
 * @param totalMeasurementsCount Number of recordings made by the user. Useful to track potential
 *                               inconsistencies between this entity and the actual stored measurements.
 * @param totalMeasuredDuration Total duration of all recorded measurements, in milliseconds.
 */
@Serializable
data class UserStatistics(
    val totalMeasurementsCount: Int = 0,
    val totalMeasuredDuration: Long = 0,
) {

    // - Constants

    companion object {

        // Use a constant identifier for this object as we're only going to store one entity.
        const val UUID = "user_statistics"
    }
}
