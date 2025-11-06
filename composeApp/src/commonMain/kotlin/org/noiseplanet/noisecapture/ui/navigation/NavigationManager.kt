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
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreen
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreenViewModel
import org.noiseplanet.noisecapture.ui.features.details.DetailsScreen
import org.noiseplanet.noisecapture.ui.features.details.DetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreen
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreen
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.map.CommunityMapScreen
import org.noiseplanet.noisecapture.ui.features.map.CommunityMapScreenViewModel
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreen
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreen
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel
import org.noiseplanet.noisecapture.ui.navigation.router.DetailsRouter
import org.noiseplanet.noisecapture.ui.navigation.router.HistoryRouter
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter
import org.noiseplanet.noisecapture.ui.navigation.router.RecordingRouter


@Composable
fun NavigationManager(
    navController: NavHostController,
    appBarState: AppBarState,
    innerPadding: PaddingValues,
    showPermissionPrompt: (Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        composable<HomeRoute> { backStackEntry ->
            val router = HomeRouter(navController, backStackEntry, showPermissionPrompt)

            val screenViewModel: HomeScreenViewModel = koinViewModel {
                parametersOf(
                    // Callback triggered when pressing the settings app bar button
                    router::onClickSettingsButton
                )
            }
            appBarState.setCurrentScreenViewModel(screenViewModel)

            HomeScreen(
                viewModel = screenViewModel,
                router = router,
            )
        }

        composable<RecordingRoute> { backStackEntry ->
            val screenViewModel: RecordingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            RecordingScreen(
                router = RecordingRouter(navController, backStackEntry)
            )
        }

        composable<HistoryRoute> { backStackEntry ->
            val screenViewModel: HistoryScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            HistoryScreen(
                viewModel = screenViewModel,
                router = HistoryRouter(navController, backStackEntry)
            )
        }

        composable<DetailsRoute> { backStackEntry ->
            val route: DetailsRoute = backStackEntry.toRoute()

            val screenViewModel: DetailsScreenViewModel = koinViewModel {
                parametersOf(route.measurementId)
            }
            appBarState.setCurrentScreenViewModel(screenViewModel)

            DetailsScreen(
                viewModel = screenViewModel,
                router = DetailsRouter(navController, backStackEntry)
            )
        }

        composable<CommunityMapRoute> { backStackEntry ->
            val screenViewModel: CommunityMapScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            CommunityMapScreen()
        }

        composable<SettingsRoute> {
            val screenViewModel: SettingsScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            SettingsScreen(screenViewModel)
        }

        composable<DebugRoute> {
            val screenViewModel: DebugScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            DebugScreen()
        }
    }
}
