package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView
import org.noiseplanet.noisecapture.ui.features.recording.controls.RecordingControls
import org.noiseplanet.noisecapture.ui.navigation.router.RecordingRouter


@OptIn(KoinExperimentalAPI::class)
@Composable
fun MeasurementRecordingScreen(
    router: RecordingRouter,
) {

    // - DI

    rememberKoinModules {
        listOf(measurementRecordingModule)
    }


    // - Layout

    Surface(
        color = MaterialTheme.colorScheme.inverseSurface
    ) {
        BoxWithConstraints(
            modifier = Modifier
        ) {
            if (maxWidth > maxHeight) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth(.5F)) {
                        SoundLevelMeterView()
                        RecordingControls(router::onMeasurementDone)
                    }
                    Column(modifier = Modifier) {
                        MeasurementRecordingPager()
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SoundLevelMeterView()
                        MeasurementRecordingPager(modifier = Modifier.fillMaxWidth().weight(1f))
                    }

                    RecordingControls(
                        onMeasurementDone = router::onMeasurementDone,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
