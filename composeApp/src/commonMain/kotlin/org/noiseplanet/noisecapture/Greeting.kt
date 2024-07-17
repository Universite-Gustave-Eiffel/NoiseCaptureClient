package org.noiseplanet.noisecapture

import getPlatform

class Greeting {

    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
