package org.noise_planet.noisecapture

actual fun createAudioSource(): AudioSource {
    return AndroidAudioSource()
}
