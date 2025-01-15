package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.home.menuitem.MenuItemViewModel
import org.noiseplanet.noisecapture.ui.navigation.Route

val homeModule = module {

    viewModel { (label: StringResource, imageVector: ImageVector, route: Route?) ->
        MenuItemViewModel(label, imageVector, route)
    }

    viewModel {
        HomeScreenViewModel()
    }
}
