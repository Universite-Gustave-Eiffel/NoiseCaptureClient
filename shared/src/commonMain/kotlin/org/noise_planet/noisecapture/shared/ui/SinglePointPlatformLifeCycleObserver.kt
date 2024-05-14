package org.noise_planet.noisecapture.shared.ui

import com.bumble.appyx.navigation.lifecycle.DefaultPlatformLifecycleObserver
import com.bumble.appyx.navigation.lifecycle.Lifecycle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


fun interface SinglePointPlatformLifeCycleObserver : DefaultPlatformLifecycleObserver {
    fun onEvent(event: Lifecycle.Event)

    override fun onCreate() {
        onEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onDestroy() {
        onEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onPause() {
        onEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onResume() {
        onEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStart() {
        onEvent(Lifecycle.Event.ON_START)
    }

    override fun onStop() {
        onEvent(Lifecycle.Event.ON_STOP)
    }


}

fun Lifecycle.asEventFlow(): Flow<Lifecycle.Event> =
    callbackFlow {
        val observer = SinglePointPlatformLifeCycleObserver{event -> trySend(event) }
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }
