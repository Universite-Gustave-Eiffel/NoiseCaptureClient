package org.noiseplanet.noisecapture.util


/**
 * Specifies parameters for in app VU meters.
 */
object VuMeterOptions {

    /**
     * Minimum dB threshold for a value to be taken into account.
     */
    const val DB_MIN: Double = 20.0

    /**
     * Maximum dB threshold for a value to be taken into account.
     */
    const val DB_MAX: Double = 120.0
}


/**
 * Tells if this value is in the range supported by in app VU meters.
 *
 * @return True if this value is in the range supported by in app VU meters.
 */
fun Double.isInVuMeterRange(): Boolean {
    return this in VuMeterOptions.DB_MIN..VuMeterOptions.DB_MAX
}
