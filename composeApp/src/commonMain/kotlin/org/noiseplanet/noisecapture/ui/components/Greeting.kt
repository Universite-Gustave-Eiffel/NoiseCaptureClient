package org.noiseplanet.noisecapture.ui.components

import getPlatform

class Greeting {

    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
