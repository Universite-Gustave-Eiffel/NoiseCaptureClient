package org.noiseplanet.noisecapture.ui.features.calibration.analysis

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.noiseplanet.noisecapture.ui.navigation.router.CalibrationRouter
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun CalibrationScreen(
    viewModel: CalibrationScreenViewModel,
    router: CalibrationRouter,
) {
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

            else -> 0f
        }
    }

    val progressIndicatorColor by animateColorAsState(
        when (viewState) {
            is CalibrationScreenViewModel.ViewState.Recording -> {
                val state = viewState as CalibrationScreenViewModel.ViewState.Recording
                NoiseLevelColorRamp.getColorForSPLValue(
                    value = state.currentAverage,
                    palette = NoiseLevelColorRamp.paletteLighter
                )
            }

            else -> NoiseLevelColorRamp.level1Light
        }
    )


    // - Layout

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            // Show countdown progress indicator based on current state value
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(color = progressIndicatorColor)
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
                    )
                    .fillMaxHeight(progressIndicatorHeightFraction)
            )

            // Depending on current state, show the corresponding value
            when (viewState) {
                is CalibrationScreenViewModel.ViewState.Countdown -> {
                    CalibrationCountdownView(
                        viewState as CalibrationScreenViewModel.ViewState.Countdown, router
                    )
                }

                is CalibrationScreenViewModel.ViewState.Recording -> {
                    CalibrationRecordingView(
                        viewState as CalibrationScreenViewModel.ViewState.Recording, router
                    )
                }

                is CalibrationScreenViewModel.ViewState.Results -> {
                    Text("DONE !")
                }
            }
        }
    }
}
