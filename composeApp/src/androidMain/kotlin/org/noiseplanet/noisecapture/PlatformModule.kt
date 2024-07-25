package org.noiseplanet.noisecapture

import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AndroidAudioSource
import org.noiseplanet.noisecapture.audio.AudioSource

/**
 * Registers koin components specific to this platform
 */
val platformModule: Module = module {
    factory<AudioSource> { AndroidAudioSource(logger = get()) }
}
