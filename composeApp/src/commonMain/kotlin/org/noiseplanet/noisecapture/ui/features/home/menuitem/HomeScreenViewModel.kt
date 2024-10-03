package org.noiseplanet.noisecapture.ui.features.home.menuitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.menu_feedback
import noisecapture.composeapp.generated.resources.menu_history
import noisecapture.composeapp.generated.resources.menu_map
import noisecapture.composeapp.generated.resources.menu_new_measurement
import noisecapture.composeapp.generated.resources.menu_settings
import noisecapture.composeapp.generated.resources.menu_statistics
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.navigation.Route

class HomeScreenViewModel : ViewModel(), KoinComponent {

    val menuItems: Array<MenuItemViewModel> = arrayOf(
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_new_measurement, Icons.Filled.Mic, Route.RequestPermission)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_history, Icons.Filled.History, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_feedback, Icons.Filled.HistoryEdu, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_statistics, Icons.Filled.Timeline, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_map, Icons.Filled.Map, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_settings, Icons.Filled.Settings, Route.Settings)
        },
    )
}
