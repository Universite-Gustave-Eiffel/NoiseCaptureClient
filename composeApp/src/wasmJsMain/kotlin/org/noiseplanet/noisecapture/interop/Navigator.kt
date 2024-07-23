package org.noiseplanet.noisecapture.interop

/**
 * Navigator Kotlin interop that adds geolocation support to the default
 * w3c.dom implementation provided with kotlin stdlib.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Navigator)
 */
abstract external class Navigator : org.w3c.dom.Navigator {

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/geolocation)
     */
    val geolocation: Geolocation?
}

/**
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/navigator)
 */
external val navigator: Navigator?
