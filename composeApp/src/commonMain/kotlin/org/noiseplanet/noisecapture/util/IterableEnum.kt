package org.noiseplanet.noisecapture.util

import kotlin.enums.EnumEntries

/**
 * This is a bit of a hack to be able to access a generic enum's entry list from any instance.
 *
 * ```kotlin
 * fun <T: Enum<T>> example() {
 *     val entries = T.entries // doesn't compile even if T is restricted to Enum
 * }
 *
 * fun <T> workaround(someValue: T) where T: Enum<T>, T: IterableEnum<T> {
 *     val entries = someValue.entries() // Accessible through an enum instance
 * }
 * ```
 */
interface IterableEnum<T : Enum<T>> {

    /**
     * Gets a list of all available enum values for this enum class.
     *
     * @return `T.entries`
     */
    fun entries(): EnumEntries<T>
}
