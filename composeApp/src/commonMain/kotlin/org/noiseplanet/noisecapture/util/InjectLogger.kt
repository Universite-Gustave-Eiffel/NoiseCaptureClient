package org.noiseplanet.noisecapture.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.emptyParametersHolder
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.log.Logger


/**
 * Utility function to inject Logger to koin component by inferring Tag automatically from calling
 * class name.
 *
 * @return Lazy [Logger] injector
 */
fun KoinComponent.injectLogger(): Lazy<Logger> {
    return inject<Logger> {
        this::class.simpleName?.let {
            parametersOf(it)
        } ?: emptyParametersHolder()
    }
}
