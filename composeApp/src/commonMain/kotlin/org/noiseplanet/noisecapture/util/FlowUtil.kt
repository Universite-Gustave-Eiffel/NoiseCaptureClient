package org.noiseplanet.noisecapture.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn


/**
 * Throttles the values emitted by this flow so that one value is emitted every fixed delay, while
 * still emitting the first available value as soon as possible if wanted.
 *
 * Note that if an element is emitted during the delay period, it will still be emitted once the
 * delay has elapsed and not be dropped. If two elements or more are emitted during the delay period,
 * only the last element will by emitted once the delay has elapsed.
 *
 * @param delayMillis Milliseconds delay between each emitted element.
 * @param emitFirstValue If true, emits the first value coming from this flow instantly, then
 *                       throttles following values.
 */
@OptIn(FlowPreview::class)
fun <T> Flow<T>.throttleLatest(delayMillis: Long, emitFirstValue: Boolean = true): Flow<T> {
    var firstEmit = emitFirstValue

    return merge(flow {
        this@throttleLatest.collect { value ->
            if (firstEmit) {
                emit(value)
                firstEmit = false
            }
        }
    }, this.sample(delayMillis))
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
