package org.noiseplanet.noisecapture.model.dao

/**
 * A record of Leq values at a given time.
 *
 * @param timestamp Record timestamp in milliseconds since epoch (UTC)
 * @param lzeq Global LZeq value
 * @param laeq Global LAeq value
 * @param lceq Global LCeq value
 * @param leqsPerThirdOctave Leq values per third octave (keyed by third octave center frequency)
 */
data class LeqRecord(
    val timestamp: Long,

    val lzeq: Double,
    val laeq: Double,
    val lceq: Double,

    val leqsPerThirdOctave: Map<Int, Double>,
)
