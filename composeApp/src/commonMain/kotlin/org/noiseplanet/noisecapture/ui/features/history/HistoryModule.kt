package org.noiseplanet.noisecapture.ui.features.history

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val historyModule = module {

    viewModel {
        HistoryScreenViewModel()
    }
}
