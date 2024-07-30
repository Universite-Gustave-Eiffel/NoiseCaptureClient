package org.noiseplanet.noisecapture

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.ui.AppBar
import org.noiseplanet.noisecapture.ui.NavigationRoute
import org.noiseplanet.noisecapture.ui.navigation.Transitions
import org.noiseplanet.noisecapture.ui.screens.HomeScreen
import org.noiseplanet.noisecapture.ui.screens.MeasurementScreen
import org.noiseplanet.noisecapture.ui.screens.PlatformInfoScreen
import org.noiseplanet.noisecapture.ui.screens.RequestPermissionScreen


/**
 * Root component of the app.
 * Currently handles the navigation stack, and navigation bar management.
 */
@Composable
fun NoiseCaptureApp(
    navController: NavHostController = rememberNavController(),
    logger: Logger = koinInject { parametersOf("NoiseCaptureApp") },
) {
    // Get current navigation back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = NavigationRoute.valueOf(
        backStackEntry?.destination?.route ?: NavigationRoute.Home.name
    )

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        // TODO: Handle swipe back gestures on iOS -> encapsulate UINavigationController?
        // TODO: Handle predictive back gestures on Android
        NavHost(
            navController = navController,
            startDestination = NavigationRoute.Home.name,
            enterTransition = Transitions.enterTransition,
            exitTransition = Transitions.exitTransition,
            popEnterTransition = Transitions.popEnterTransition,
            popExitTransition = Transitions.popExitTransition,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = NavigationRoute.Home.name) {
                HomeScreen(
                    onClick = {
                        // TODO: Silently check for permissions and bypass this step if they are already all granted
                        navController.navigate(NavigationRoute.RequestPermission.name)
                    },
                )
            }
            composable(route = NavigationRoute.PlatformInfo.name) {
                PlatformInfoScreen(
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = NavigationRoute.RequestPermission.name) {
                RequestPermissionScreen(
                    onClickNextButton = {
                        navController.navigate(NavigationRoute.Measurement.name)
                    }
                )
            }
            composable(route = NavigationRoute.Measurement.name) {
                // TODO: Decide of a standard for screens architecture:
                //       - class or compose function as root?
                //       - Inject dependencies in constructor or via Koin factories?
                //       - What should be the package structure?
                MeasurementScreen(measurementService = koinInject())
                    .Content()
            }
        }
    }
}
