package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController


class OnboardingRouter(
    navController: NavHostController,
    val goToNextStep: () -> Unit,
) : Router(navController)
