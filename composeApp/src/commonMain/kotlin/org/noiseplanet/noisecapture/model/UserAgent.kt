package org.noiseplanet.noisecapture.model

import kotlinx.serialization.Serializable
import org.noiseplanet.noisecapture.BuildKonfig

/**
 * Information about the currently running app, as well as the device
 * it is running on.
 */
@Serializable
data class UserAgent(

    /**
     * App version name (e.g. "1.3.7")
     */
    val versionName: String = BuildKonfig.versionName,

    /**
     * App version number (e.g. 34)
     */
    val versionCode: Int = BuildKonfig.versionCode,

    /**
     * Name of the manufacturer of the device, if available (e.g. "Apple")
     */
    val deviceManufacturer: String?,

    /**
     * Name of the device model, if available (e.g. "Galaxy S24")
     */
    val deviceModelName: String?,

    /**
     * Manufacturer code for the device model, if available (e.g. "Galaxy_S24_AE")
     */
    val deviceModelCode: String?,

    /**
     * Name of operating system, if available (e.g. "macOS")
     */
    val osName: String?,

    /**
     * Operating system version, if available (e.g. "17.8")
     */
    val osVersion: String?,
)
