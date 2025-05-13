package org.noiseplanet.noisecapture.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.appbar.AppBar
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.components.appbar.rememberAppBarState
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreen
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreen
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreen
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreenViewModel
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionScreen
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreen
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel


/**
 * Root component of the app.
 * Currently handles the navigation stack, and navigation bar management.
 */
@Composable
fun RootCoordinator(
    viewModel: RootCoordinatorViewModel,
) {

    // - Properties

    val navController: NavHostController = rememberNavController()
    val appBarState: AppBarState = rememberAppBarState(navController)

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current


    // - Lifecycle

    navController.addOnDestinationChangedListener { _, destination, _ ->
        // Triggered when navigating to a new screen or from another screen
        destination.route?.let {
            viewModel.toggleAudioSourceForScreen(it)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    // When the app is launched, setup audio source.
                    viewModel.setupAudioSource()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    // When app is sent to background, pause audio source if not recording
                    viewModel.stopAudioSourceIfNotRecording()
                }

                Lifecycle.Event.ON_RESUME -> {
                    // When app comes back to foreground and the current screen uses incoming
                    // audio, resume audio source if not recording
                    navController.currentDestination?.route?.let {
                        viewModel.toggleAudioSourceForScreen(it)
                    }
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            if (viewModel.isRecording) {
                // If a recording is currently ongoing when the app stops, end recording to ensure
                // that results are saved properly.
                viewModel.endRecording()
            }
            viewModel.releaseAudioSource()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Navigation

    Scaffold(
        topBar = {
            AppBar(appBarState)
        }
    ) { innerPadding ->
        // TODO: Handle swipe back gestures on iOS -> encapsulate UINavigationController?
        NavHost(
            navController = navController,
            startDestination = Route.RequestPermission.name,
            enterTransition = Transitions.enterTransition,
            exitTransition = Transitions.exitTransition,
            popEnterTransition = Transitions.popEnterTransition,
            popExitTransition = Transitions.popExitTransition,
            modifier = Modifier.fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(Color.White)
        ) {
            composable(route = Route.Home.name) {
                val screenViewModel: HomeScreenViewModel = koinInject {
                    parametersOf({
                        // Callback triggered when pressing the settings app bar button
                        navController.navigate(Route.Settings.name)
                    }, {
                        // Callback triggered when pressing the open sound level meter button
                        navController.navigate(Route.Measurement.name)
                    }, { _: Measurement ->
                        // Callback triggered when clicking a measurement
                        // TODO: Open measurement details
                    }, {
                        // Callback triggered when clicking the open history button or card
                        navController.navigate(Route.History.name)
                    })
                }
                appBarState.setCurrentScreenViewModel(screenViewModel)

                HomeScreen(viewModel = screenViewModel)
            }

            composable(route = Route.RequestPermission.name) {
                // TODO: Silently check for permissions and bypass this step if
                //       they are already all granted
                val screenViewModel: RequestPermissionScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                RequestPermissionScreen(
                    viewModel = screenViewModel,
                    onClickNextButton = {
                        navController.navigate(Route.Home.name)
                    }
                )
            }

            composable(route = Route.Measurement.name) {
                val screenViewModel: MeasurementScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                MeasurementScreen(screenViewModel)
            }

            composable(route = Route.History.name) {
                val screenViewModel: HistoryScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                HistoryScreen(screenViewModel)
            }

            composable(route = Route.Settings.name) {
                val screenViewModel: SettingsScreenViewModel = koinInject()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                SettingsScreen(screenViewModel)
            }
        }
    }
}
