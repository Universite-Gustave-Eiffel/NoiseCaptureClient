package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import org.noiseplanet.noisecapture.ui.navigation.HomeRoute
import org.noiseplanet.noisecapture.ui.navigation.OnboardingAcousticsKnowledgeRoute
import org.noiseplanet.noisecapture.ui.navigation.OnboardingHowItWorksRoute
import org.noiseplanet.noisecapture.ui.navigation.OnboardingLocationPermissionRoute
import org.noiseplanet.noisecapture.ui.navigation.OnboardingMicPermissionRoute
import org.noiseplanet.noisecapture.ui.navigation.OnboardingWelcomeRoute
import org.noiseplanet.noisecapture.ui.navigation.Route


class OnboardingRouter(navController: NavHostController) : Router(navController) {

    // - Properties

    private val steps: List<Route> = listOf(
        OnboardingWelcomeRoute(),
        OnboardingHowItWorksRoute(),
        OnboardingAcousticsKnowledgeRoute(),
        OnboardingMicPermissionRoute(),
        OnboardingLocationPermissionRoute(),
    )


    // - Public functions

    fun goToNextStep() {
        val currentRoute = navController.currentBackStackEntry?.toRoute<Route>() ?: return
        val currentStepIndex = steps.indexOfFirst { it.id == currentRoute.id }
        val nextStepIndex = currentStepIndex + 1

        if (nextStepIndex == steps.size) {
            // If onboarding is over, navigate to home screen
            navController.navigate(HomeRoute()) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(steps[nextStepIndex])
        }
    }
}
