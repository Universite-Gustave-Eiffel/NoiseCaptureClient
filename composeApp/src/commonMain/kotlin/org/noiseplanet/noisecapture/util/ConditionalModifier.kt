package org.noiseplanet.noisecapture.util

import androidx.compose.ui.Modifier

/**
 * Utility modifier to apply modifiers based on a given predicate
 *
 * @param predicate Input condition
 * @param ifTrue Modifier that will be returned if predicate evaluates to `true`
 * @param ifTrue Modifier that will be returned if predicate evaluates to `false`.
 *               By default, will just return `this`.
 *
 * @return Updated [Modifier]
 */
inline fun Modifier.conditional(
    predicate: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: Modifier.() -> Modifier = { this },
): Modifier = if (predicate) {
    then(ifTrue(Modifier))
} else {
    then(ifFalse(Modifier))
}
