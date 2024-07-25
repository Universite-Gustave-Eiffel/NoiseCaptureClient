package org.noiseplanet.noisecapture

import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.JsAudioSource

val platformModule: Module = module {
    factory<AudioSource> { JsAudioSource() }
}
