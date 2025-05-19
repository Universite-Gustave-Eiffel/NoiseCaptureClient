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
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.appbar.AppBar
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.components.appbar.rememberAppBarState
import org.noiseplanet.noisecapture.ui.features.details.MeasurementDetailsScreen
import org.noiseplanet.noisecapture.ui.features.details.MeasurementDetailsScreenViewModel
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

    navController.addOnDestinationChangedListener { navController, _, _ ->
        // Triggered when navigating to a new screen or from another screen
        navController.currentBackStackEntry?.toRoute<Route>()?.let {
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
                    navController.currentBackStackEntry?.toRoute<Route>()?.let {
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
            startDestination = RequestPermissionRoute(),
            enterTransition = Transitions.enterTransition,
            exitTransition = Transitions.exitTransition,
            popEnterTransition = Transitions.popEnterTransition,
            popExitTransition = Transitions.popExitTransition,
            modifier = Modifier.fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(Color.White)
        ) {
            composable<HomeRoute> {
                val screenViewModel: HomeScreenViewModel = koinViewModel {
                    parametersOf({
                        // Callback triggered when pressing the settings app bar button
                        navController.navigate(SettingsRoute())
                    })
                }
                appBarState.setCurrentScreenViewModel(screenViewModel)

                HomeScreen(
                    viewModel = screenViewModel,
                    onClickMeasurement = { measurement: Measurement ->
                        navController.navigate(MeasurementDetailsRoute(measurement.uuid))
                    },
                    onClickOpenSoundLevelMeterButton = {
                        navController.navigate(MeasurementRecordingRoute())
                    },
                    onClickOpenHistoryButton = {
                        navController.navigate(HistoryRoute())
                    },
                )
            }

            composable<RequestPermissionRoute> {
                // TODO: Silently check for permissions and bypass this step if
                //       they are already all granted
                val screenViewModel: RequestPermissionScreenViewModel = koinViewModel()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                RequestPermissionScreen(
                    viewModel = screenViewModel,
                    onClickNextButton = {
                        navController.navigate(HomeRoute())
                    }
                )
            }

            composable<MeasurementRecordingRoute> {
                val screenViewModel: MeasurementScreenViewModel = koinViewModel()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                MeasurementScreen()
            }

            composable<HistoryRoute> {
                val screenViewModel: HistoryScreenViewModel = koinViewModel()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                HistoryScreen(screenViewModel)
            }

            composable<MeasurementDetailsRoute> { backstackEntry ->
                val route: MeasurementDetailsRoute = backstackEntry.toRoute()

                val screenViewModel: MeasurementDetailsScreenViewModel = koinViewModel {
                    parametersOf(route.measurementId)
                }
                appBarState.setCurrentScreenViewModel(screenViewModel)

                MeasurementDetailsScreen(
                    viewModel = screenViewModel,
                    onMeasurementDeleted = {
                        navController.popBackStack()
                    }
                )
            }

            composable<SettingsRoute> {
                val screenViewModel: SettingsScreenViewModel = koinViewModel()
                appBarState.setCurrentScreenViewModel(screenViewModel)

                SettingsScreen(screenViewModel)
            }
        }
    }
}
