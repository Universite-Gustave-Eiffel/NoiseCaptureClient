package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.onboarding_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class OnboardingScreenViewModel : ScreenViewModel, ViewModel() {

    // - Properties

    override val title: StringResource = Res.string.onboarding_title
}
