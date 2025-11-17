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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
    router: RecordingRouter,
) {

    // - DI

    rememberKoinModules {
        listOf(recordingModule)
    }


    // - Properties

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass


    // - Layout

    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        if (sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) {
            RecordingScreenCompact(router)
        } else if (sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) {
            RecordingScreenMedium(router)
        } else {
            RecordingScreenLarge(router)
        }
    }
}


@Composable
private fun RecordingScreenCompact(router: RecordingRouter) {
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
            onMeasurementDone = router::onMeasurementDone,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp)
        )
    }
}


@Composable
private fun RecordingScreenMedium(router: RecordingRouter) {
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
                onMeasurementDone = router::onMeasurementDone,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Composable
private fun RecordingScreenLarge(router: RecordingRouter) {
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
                onMeasurementDone = router::onMeasurementDone,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
