package org.noiseplanet.noisecapture.ui.features.calibration

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.navigation.router.CalibrationRouter
import org.noiseplanet.noisecapture.ui.theme.Noise


@OptIn(KoinExperimentalAPI::class)
@Composable
fun CalibrationScreen(
    viewModel: CalibrationScreenViewModel,
    router: CalibrationRouter,
) {
    // - DI

    rememberKoinModules {
        listOf(calibrationModule)
    }


    // - Properties

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    val progressIndicatorHeightFraction by derivedStateOf {
        when (viewState) {
            is CalibrationScreenViewModel.ViewState.Countdown -> {
                val state = viewState as CalibrationScreenViewModel.ViewState.Countdown
                1f - (state.timeLeft / state.duration).toFloat()
            }

            is CalibrationScreenViewModel.ViewState.Recording -> {
                val state = viewState as CalibrationScreenViewModel.ViewState.Recording
                (state.timeLeft / state.duration).toFloat()
            }

            is CalibrationScreenViewModel.ViewState.Configure -> 1f
            is CalibrationScreenViewModel.ViewState.Results -> 0f
        }
    }
    val animationDurationMs by derivedStateOf {
        if (progressIndicatorHeightFraction == 0f) 0 else 150
    }


    // - Layout

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
    ) {
        // When navigating backwards, return to configuration screen if calibration is ongoing
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = true,
            onBackCompleted = {
                if (viewModel.confirmPopBackStack()) {
                    router.popBackStack()
                }
            }
        )

        Box(
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Show countdown progress indicator based on current state value
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(color = Color.Noise.one.light)
                    .animateContentSize(
                        tween(
                            durationMillis = animationDurationMs,
                            easing = LinearEasing
                        )
                    )
                    .fillMaxHeight(progressIndicatorHeightFraction)
            )

            // Depending on current state, show the corresponding value
            when (viewState) {
                is CalibrationScreenViewModel.ViewState.Configure -> {
                    CalibrationConfigView(
                        viewModel, viewState as CalibrationScreenViewModel.ViewState.Configure
                    )
                }

                is CalibrationScreenViewModel.ViewState.Countdown -> {
                    CalibrationCountdownView(
                        viewModel, viewState as CalibrationScreenViewModel.ViewState.Countdown
                    )
                }

                is CalibrationScreenViewModel.ViewState.Recording -> {
                    CalibrationRecordingView(
                        viewModel, viewState as CalibrationScreenViewModel.ViewState.Recording
                    )
                }

                is CalibrationScreenViewModel.ViewState.Results -> {
                    CalibrationResultsView(
                        viewModel, viewState as CalibrationScreenViewModel.ViewState.Results, router
                    )
                }
            }
        }
    }
}
