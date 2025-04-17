package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * For compression reasons when storing values in JSON format, we want to group values
 * into a representation where each property is an array of values to spare writing the
 * keys everytime.
 *
 * > TODO: Figure out a neat way to expose immutable versions of the lists using a backing
 *         property or something, and only leave a public method to push a new record to
 *         the sequence fragment.
 *         Look into explicit backing fields introduced in Kotlin 2.0 when K2 mode will be supported
 *         by the compose multiplatform plugin.
 *         https://github.com/Kotlin/KEEP/blob/explicit-backing-fields-re/proposals/explicit-backing-fields.md
 */
@Serializable
@OptIn(ExperimentalUuidApi::class)
data class LeqSequenceFragment(
    override val uuid: String = Uuid.random().toString(),
    override val measurementId: String,
    override val timestamp: MutableList<Long> = mutableListOf(),

    val lzeq: MutableList<Double> = mutableListOf(),
    val laeq: MutableList<Double> = mutableListOf(),
    val lceq: MutableList<Double> = mutableListOf(),

    val leqsPerThirdOctaveBand: MutableMap<Int, MutableList<Double>> = mutableMapOf(),
) : SequenceFragment<LeqRecord> {

    override fun push(element: LeqRecord) {
        timestamp.add(element.timestamp)
        lzeq.add(element.lzeq)
        laeq.add(element.laeq)
        lceq.add(element.lzeq)

        element.leqsPerThirdOctave.forEach { entry ->
            // For each third octave band, if the frequency already exists in the map,
            // simply add the leq value to its list. Otherwise, create a new mutable list
            // with the leq value as sole element.
            leqsPerThirdOctaveBand[entry.key]?.apply {
                add(entry.value)
            } ?: run {
                leqsPerThirdOctaveBand[entry.key] = mutableListOf(entry.value)
            }
        }
    }
}
