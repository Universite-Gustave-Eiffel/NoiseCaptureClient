package org.noiseplanet.noisecapture.ui.features.history

import org.koin.dsl.module

val historyModule = module {

    factory { (measurementId: String) ->
        HistoryItemViewModel(measurementId)
    }
}
