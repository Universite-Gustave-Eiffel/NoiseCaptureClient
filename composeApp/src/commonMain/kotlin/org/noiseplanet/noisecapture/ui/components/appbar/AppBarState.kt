package org.noiseplanet.noisecapture.ui.components.appbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

@Stable
class AppBarState(
    val navController: NavHostController,
) {

    var viewModel by mutableStateOf<ScreenViewModel?>(null)
        private set

    fun setCurrentScreenViewModel(viewModel: ScreenViewModel) {
        this.viewModel = viewModel
    }

    val isVisible: Boolean
        get() = viewModel?.isAppBarVisible == true
}


@Composable
fun rememberAppBarState(
    navController: NavHostController,
) = remember {
    AppBarState(navController)
}
