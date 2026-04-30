package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.onboarding_continue
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter


@Composable
fun OnboardingAcousticsKnowledgeScreen(
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {

    // - Layout

    OnboardingScreenContainer(
        primaryButtonTitle = Res.string.onboarding_continue,
        primaryButtonAction = { router.goToNextStep() },
        modifier = modifier,
    ) {
        Text("Acoustics knowledge")
    }
}
