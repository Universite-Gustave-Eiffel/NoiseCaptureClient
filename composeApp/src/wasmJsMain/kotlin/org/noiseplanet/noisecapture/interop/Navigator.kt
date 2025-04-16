package org.noiseplanet.noisecapture.interop

import org.noiseplanet.noisecapture.interop.storage.StorageManager

/**
 * Navigator Kotlin interop that adds geolocation and storage support to the default
 * w3c.dom implementation provided with kotlin stdlib.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Navigator)
 */
abstract external class Navigator : org.w3c.dom.Navigator {

    /**
     * Returns a [Geolocation] object that gives Web content access to the location of the device.
     * This allows a website or app to offer customized results based on the user's location.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/geolocation)
     */
    val geolocation: Geolocation?

    /**
     * Returns the singleton [StorageManager] object used to access the overall storage capabilities
     * of the browser for the current site or app. The returned object lets you examine and configure
     * persistence of data stores and learn approximately how much more space your browser has
     * available for local storage use.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/storage)
     */
    val storage: StorageManager
}

/**
 * The Navigator interface represents the state and the identity of the user agent.
 * It allows scripts to query it and to register themselves to carry on some activities.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/navigator)
 */
external val navigator: Navigator?
