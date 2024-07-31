package org.noiseplanet.noisecapture.ui.features.home.menuitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.menu_history
import noisecapture.composeapp.generated.resources.menu_new_measurement
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.navigation.Route

class HomeScreenViewModel : ViewModel(), KoinComponent {

    val menuItems: Array<MenuItemViewModel> = arrayOf(
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_new_measurement, Icons.Filled.Mic, Route.Measurement)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_history, Icons.Filled.History, null)
        },
    )
}
