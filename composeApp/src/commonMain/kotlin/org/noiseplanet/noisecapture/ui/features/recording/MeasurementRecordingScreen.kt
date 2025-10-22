package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(measurementRecordingModule)
    }


    // - Layout

    Surface(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.surface
    ) {
        BoxWithConstraints {
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
                Column {
                    SoundLevelMeterView()
                    MeasurementRecordingPager(modifier = Modifier.fillMaxWidth().weight(1f))
                    RecordingControls(router::onMeasurementDone)
                }
            }
        }
    }
}
