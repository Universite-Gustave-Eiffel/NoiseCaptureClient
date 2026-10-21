package org.noiseplanet.noisecapture.ui.features

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.calibration.CalibrationScreenViewModel
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreenViewModel
import org.noiseplanet.noisecapture.ui.features.details.DetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.map.CommunityMapScreenViewModel
import org.noiseplanet.noisecapture.ui.features.onboarding.OnboardingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.questionnaire.QuestionnaireScreenViewModel
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute
import org.noiseplanet.noisecapture.ui.navigation.QuestionnaireRoute


val screensModule = module {

    viewModel { (onClickSettingsButton: () -> Unit) ->
        HomeScreenViewModel(
            onClickSettingsButton = onClickSettingsButton,
        )
    }

    viewModel {
        RecordingScreenViewModel()
    }

    viewModel { (route: DetailsRoute) ->
        DetailsScreenViewModel(route.measurementId)
    }

    viewModel {
        HistoryScreenViewModel()
    }

    viewModel {
        SettingsScreenViewModel()
    }

    viewModel {
        CommunityMapScreenViewModel()
    }

    viewModel {
        CalibrationScreenViewModel()
    }

    viewModel {
        OnboardingScreenViewModel()
    }

    viewModel { (route: QuestionnaireRoute) ->
        QuestionnaireScreenViewModel(
            measurementId = route.measurementId,
            questionnaireId = route.questionnaireId,
            isReviewMode = route.isReviewMode,
        )
    }

    viewModel {
        DebugScreenViewModel()
    }
}
