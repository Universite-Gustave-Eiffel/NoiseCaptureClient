package org.noiseplanet.noisecapture.util

import org.jetbrains.compose.resources.StringResource

/**
 * Defines a data object that can be represented by an shortened name and full name.
 * Useful for dropdown menus for instance.
 */
interface ShortNameRepresentable {

    val shortName: StringResource
    val fullName: StringResource
}
