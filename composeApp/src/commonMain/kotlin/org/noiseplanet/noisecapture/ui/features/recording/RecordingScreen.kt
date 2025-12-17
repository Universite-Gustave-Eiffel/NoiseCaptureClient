@file:OptIn(ExperimentalComposeUiApi::class)

package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.window.core.layout.WindowSizeClass
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.map.MapView
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView
import org.noiseplanet.noisecapture.ui.features.recording.controls.RecordingControls
import org.noiseplanet.noisecapture.ui.navigation.router.RecordingRouter


@OptIn(KoinExperimentalAPI::class)
@Composable
fun RecordingScreen(
    viewModel: RecordingScreenViewModel,
    router: RecordingRouter,
) {

    // - DI

    rememberKoinModules {
        listOf(recordingModule)
    }


    // - Properties

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass

    var showEndRecordingConfirmationDialog by remember { mutableStateOf(false) }

    viewModel.showEndRecordingConfirmationDialog = {
        showEndRecordingConfirmationDialog = true
    }


    // - Lifecycle

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.registerMeasurementDoneListener { measurementUuid ->
                        showEndRecordingConfirmationDialog = false

                        if (viewModel.shouldOpenDetailsOnceDone) {
                            router.openMeasurementDetails(measurementUuid)
                        } else {
                            router.popBackStack()
                            viewModel.shouldOpenDetailsOnceDone = true
                        }
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.deregisterMeasurementDoneListener()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Layout

    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        BackHandler {
            viewModel.confirmPopBackStack()
        }

        if (sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) {
            RecordingScreenCompact(viewModel)
        } else if (sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) {
            RecordingScreenMedium(viewModel)
        } else {
            RecordingScreenLarge(viewModel)
        }
    }

    if (showEndRecordingConfirmationDialog) {
        EndRecordingConfirmationDialog(
            onDismissRequest = { showEndRecordingConfirmationDialog = false },
            onConfirm = {
                viewModel.endCurrentRecording()
            },
        )
    }
}


@Composable
private fun RecordingScreenCompact(viewModel: RecordingScreenViewModel) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.background(color = MaterialTheme.colorScheme.inverseSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SoundLevelMeterView()
            RecordingPager(modifier = Modifier.fillMaxWidth().weight(1f))
        }
        RecordingControls(
            onStopRecording = { viewModel.showEndRecordingConfirmationDialog?.invoke() },
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp)
        )
    }
}


@Composable
private fun RecordingScreenMedium(viewModel: RecordingScreenViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp)
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            ),
    ) {
        Column(
            modifier = Modifier.weight(3f)
                .clip(shape = MaterialTheme.shapes.large)
                .background(color = MaterialTheme.colorScheme.inverseSurface)
        ) {
            SoundLevelMeterView()
            RecordingPager()
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.weight(2f)
                .fillMaxHeight()
                .clip(shape = MaterialTheme.shapes.large)
        ) {
            MapView(modifier = Modifier.fillMaxSize())

            RecordingControls(
                onStopRecording = { viewModel.showEndRecordingConfirmationDialog?.invoke() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Composable
private fun RecordingScreenLarge(viewModel: RecordingScreenViewModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp)
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            ),
    ) {
        Column(
            modifier = Modifier.weight(1f)
                .clip(shape = MaterialTheme.shapes.large)
                .background(color = MaterialTheme.colorScheme.inverseSurface)
        ) {
            SoundLevelMeterView()
            RecordingPager()
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.weight(1f)
                .fillMaxHeight()
                .clip(shape = MaterialTheme.shapes.large)
        ) {
            MapView(modifier = Modifier.fillMaxSize())

            RecordingControls(
                onStopRecording = { viewModel.showEndRecordingConfirmationDialog?.invoke() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
