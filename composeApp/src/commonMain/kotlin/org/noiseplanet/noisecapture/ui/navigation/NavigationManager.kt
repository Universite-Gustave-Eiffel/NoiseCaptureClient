package org.noiseplanet.noisecapture.ui.navigation

import Platform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.features.calibration.CalibrationScreen
import org.noiseplanet.noisecapture.ui.features.calibration.CalibrationScreenViewModel
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
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingAcousticsKnowledgeScreen
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingHowItWorksScreen
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingLocationPermissionScreen
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingMicPermissionScreen
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingWelcomeScreen
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreen
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreen
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel
import org.noiseplanet.noisecapture.ui.navigation.router.CalibrationRouter
import org.noiseplanet.noisecapture.ui.navigation.router.DetailsRouter
import org.noiseplanet.noisecapture.ui.navigation.router.HistoryRouter
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter
import org.noiseplanet.noisecapture.ui.navigation.router.RecordingRouter


@Composable
fun NavigationManager(
    navController: NavHostController,
    appBarState: AppBarState,
    innerPadding: PaddingValues,
    showPermissionPrompt: (Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val platform: Platform = koinInject()
    val onboardingRouter = remember { OnboardingRouter(navController) }


    // - Navigation graph

    NavHost(
        navController = navController,
        startDestination = OnboardingWelcomeRoute(),
        enterTransition = platform.navigationTransitions.enterTransition,
        exitTransition = platform.navigationTransitions.exitTransition,
        popEnterTransition = platform.navigationTransitions.popEnterTransition,
        popExitTransition = platform.navigationTransitions.popExitTransition,
        modifier = modifier.fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        composable<HomeRoute> {
            val router = HomeRouter(navController, showPermissionPrompt)

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

        composable<RecordingRoute> {
            val screenViewModel: RecordingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            RecordingScreen(
                viewModel = screenViewModel,
                router = RecordingRouter(navController)
            )
        }

        composable<HistoryRoute> {
            val screenViewModel: HistoryScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            HistoryScreen(
                viewModel = screenViewModel,
                router = HistoryRouter(navController)
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
                router = DetailsRouter(navController)
            )
        }

        composable<CommunityMapRoute> {
            val screenViewModel: CommunityMapScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            CommunityMapScreen()
        }

        composable<SettingsRoute> {
            val screenViewModel: SettingsScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            SettingsScreen(screenViewModel)
        }

        composable<CalibrationRoute> {
            val screenViewModel: CalibrationScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            CalibrationScreen(
                viewModel = screenViewModel,
                router = CalibrationRouter(navController)
            )
        }

        composable<OnboardingWelcomeRoute> {
            val screenViewModel: OnboardingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            OnboardingWelcomeScreen(router = onboardingRouter)
        }

        composable<OnboardingHowItWorksRoute> {
            val screenViewModel: OnboardingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            OnboardingHowItWorksScreen(router = onboardingRouter)
        }

        composable<OnboardingAcousticsKnowledgeRoute> {
            val screenViewModel: OnboardingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            OnboardingAcousticsKnowledgeScreen(
                viewModel = screenViewModel,
                router = onboardingRouter,
            )
        }

        composable<OnboardingMicPermissionRoute> {
            val screenViewModel: OnboardingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            OnboardingMicPermissionScreen(router = onboardingRouter)
        }

        composable<OnboardingLocationPermissionRoute> {
            val screenViewModel: OnboardingScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            OnboardingLocationPermissionScreen(router = onboardingRouter)
        }

        composable<DebugRoute> {
            val screenViewModel: DebugScreenViewModel = koinViewModel()
            appBarState.setCurrentScreenViewModel(screenViewModel)

            DebugScreen()
        }
    }
}
