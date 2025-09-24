package org.noiseplanet.noisecapture.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreen
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreenViewModel
import org.noiseplanet.noisecapture.ui.features.details.MeasurementDetailsScreen
import org.noiseplanet.noisecapture.ui.features.details.MeasurementDetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.MeasurementHistoryScreen
import org.noiseplanet.noisecapture.ui.features.history.MeasurementHistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreen
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.recording.MeasurementRecordingScreen
import org.noiseplanet.noisecapture.ui.features.recording.MeasurementRecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreen
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel


@Composable
fun NavigationManager(
    navController: NavHostController,
    appBarState: AppBarState,
    innerPadding: PaddingValues,
    showPermissionPrompt: (Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: Handle swipe back gestures on iOS -> encapsulate UINavigationController?
    NavHost(
        navController = navController,
        startDestination = HomeRoute(),
        enterTransition = Transitions.enterTransition,
        exitTransition = Transitions.exitTransition,
        popEnterTransition = Transitions.popEnterTransition,
        popExitTransition = Transitions.popExitTransition,
        modifier = modifier.fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        composable<HomeRoute> { backstackEntry ->
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
                    navController.navigate(
                        MeasurementDetailsRoute(
                            measurement.uuid,
                            backstackEntry.id,
                        )
                    )
                },
                onClickOpenSoundLevelMeterButton = {
                    navController.navigate(MeasurementRecordingRoute())
                },
                onClickOpenHistoryButton = {
                    navController.navigate(HistoryRoute())
                },
                showPermissionPrompt = showPermissionPrompt,
            )
        }

        composable<MeasurementRecordingRoute> { backstackEntry ->
            val screenViewModel: MeasurementRecordingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            MeasurementRecordingScreen(
                onMeasurementDone = { uuid ->
                    navController.navigate(
                        MeasurementDetailsRoute(
                            measurementId = uuid,
                            parentRouteId = backstackEntry.id
                        )
                    )
                }
            )
        }

        composable<HistoryRoute> { backstackEntry ->
            val screenViewModel: MeasurementHistoryScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            MeasurementHistoryScreen(
                screenViewModel,
                onClickMeasurement = { measurement ->
                    navController.navigate(
                        route = MeasurementDetailsRoute(
                            measurementId = measurement.uuid,
                            parentRouteId = backstackEntry.id,
                        )
                    )
                }
            )
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
                    if (navController.previousBackStackEntry?.id == route.parentRouteId) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable<SettingsRoute> {
            val screenViewModel: SettingsScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            SettingsScreen(screenViewModel)
        }

        composable<DebugRoute> {
            val screenViewModel: DebugScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            DebugScreen(screenViewModel)
        }
    }
}
