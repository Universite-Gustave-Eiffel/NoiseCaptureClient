package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_button_request
import noisecapture.composeapp.generated.resources.request_permission_button_skip
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter


@Composable
fun OnboardingMicPermissionScreen(
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {

    // - Layout

    OnboardingScreenContainer(
        primaryButtonTitle = Res.string.request_permission_button_request,
        primaryButtonAction = { router.goToNextStep() },
        secondaryButtonTitle = Res.string.request_permission_button_skip,
        secondaryButtonAction = { router.goToNextStep() },
        modifier = modifier,
    ) {
        Text("Mic permission")
    }
}
