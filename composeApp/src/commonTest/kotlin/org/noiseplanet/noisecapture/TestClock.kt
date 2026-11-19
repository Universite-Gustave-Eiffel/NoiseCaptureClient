package org.noiseplanet.noisecapture

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Clock
import kotlin.time.Instant


class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun now(): Instant {
        return Instant.fromEpochMilliseconds(scheduler.currentTime)
    }
}
