package org.noise_planet.noisecapture.shared

import kotlin.test.Test
import androidx.compose.ui.test.*
import androidx.compose.ui.text.rememberTextMeasurer
import org.noise_planet.noisecapture.shared.child.MeasurementScreen

class TestPlotGeneration {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun myTest() = runComposeUiTest {
        setContent {
            val textMeasurer = rememberTextMeasurer()
            val legendElements = MeasurementScreen.Companion.makeXLabels(
                textMeasurer, 5.0, 95.0, 1000F,
                MeasurementScreen.Companion::noiseLevelAxisFormater
            )
        }
    }

}