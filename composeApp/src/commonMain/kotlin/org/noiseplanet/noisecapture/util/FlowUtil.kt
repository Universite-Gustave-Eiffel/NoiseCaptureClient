package org.noiseplanet.noisecapture.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform


/**
 * Throttles the values emitted by this flow so that one value is emitted every fixed delay.
 * Note that if an element is emitted during the delay period, it will still be emitted once the
 * delay has elapsed and not be dropped. If two elements or more are emitted during the delay period,
 * only the last element will by emitted once the delay has elapsed.
 *
 * @param delayMillis Milliseconds delay between each emitted element.
 */
fun <T> Flow<T>.throttleLatest(delayMillis: Long) = this
    .conflate()
    .transform {
        emit(it)
        delay(delayMillis)
    }


/**
 * Utility function to use [Flow.stateIn] with [SharingStarted.WhileSubscribed] without
 * specifying it everytime.
 */
fun <T> Flow<T>.stateInWhileSubscribed(
    scope: CoroutineScope,
    initialValue: T,
    stopTimeoutMillis: Long = 5_000,
) = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
    initialValue = initialValue,
)
