package org.noiseplanet.noisecapture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.appbar.AppBar
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.components.appbar.rememberAppBarState
import org.noiseplanet.noisecapture.ui.features.home.HomeScreen
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreen
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreenViewModel
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionScreen
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreen
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel
import org.noiseplanet.noisecapture.ui.navigation.Route
import org.noiseplanet.noisecapture.ui.navigation.Transitions


/**
 * Root component of the app.
 * Currently handles the navigation stack, and navigation bar management.
 */
@Composable
fun Coordinator() {

    val navController: NavHostController = rememberNavController()
    val appBarState: AppBarState = rememberAppBarState(navController)

    Scaffold(
        topBar = {
            AppBar(appBarState)
        }
    ) { innerPadding ->
        // TODO: Handle swipe back gestures on iOS -> encapsulate UINavigationController?
        NavHost(
            navController = navController,
            startDestination = Route.Home.name,
            enterTransition = Transitions.enterTransition,
            exitTransition = Transitions.exitTransition,
            popEnterTransition = Transitions.popEnterTransition,
            popExitTransition = Transitions.popExitTransition,
            modifier = Modifier.fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(Color.White)
        ) {
            composable(route = Route.Home.name) {
                val viewModel: HomeScreenViewModel = koinInject {
                    parametersOf({ navController.navigate(Route.Settings.name) })
                }
                appBarState.setCurrentScreenViewModel(viewModel)

                HomeScreen(viewModel = viewModel, onOpenSoundLevelMeterButtonClick = {
                    navController.navigate(Route.Measurement.name)
                })
            }

            composable(route = Route.RequestPermission.name) {
                // TODO: Silently check for permissions and bypass this step if
                //       they are already all granted
                val viewModel: RequestPermissionScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(viewModel)

                RequestPermissionScreen(
                    viewModel = viewModel,
                    onClickNextButton = {
                        navController.navigate(Route.Measurement.name)
                    }
                )
            }

            composable(route = Route.Measurement.name) {
                val viewModel: MeasurementScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(viewModel)

                MeasurementScreen(viewModel)
            }

            composable(route = Route.Settings.name) {
                val viewModel: SettingsScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(viewModel)

                SettingsScreen(viewModel)
            }
        }
    }
}
