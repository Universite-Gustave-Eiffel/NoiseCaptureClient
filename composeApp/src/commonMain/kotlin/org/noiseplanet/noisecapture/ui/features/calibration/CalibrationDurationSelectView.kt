package org.noiseplanet.noisecapture.ui.features.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.add
import noisecapture.composeapp.generated.resources.remove
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds


@Composable
fun CalibrationDurationSelectView(
    onSelectedDurationChange: (durationSeconds: Int) -> Unit,
    initialValue: Int = 10,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val minCalibrationDuration: Int = 5
    val maxCalibrationDuration: Int = 30

    var selectedDurationSeconds: Int by remember { mutableStateOf(initialValue) }
    val isMinusButtonEnabled: Boolean by derivedStateOf {
        selectedDurationSeconds > minCalibrationDuration
    }
    val isPlusButtonEnabled: Boolean by derivedStateOf {
        selectedDurationSeconds < maxCalibrationDuration
    }


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        NCButton(
            content = ButtonContent(icon = Res.drawable.remove),
            colors = Color.Noise.two.secondaryContainerColors(),
            onClick = {
                selectedDurationSeconds = max(selectedDurationSeconds - 1, minCalibrationDuration)
                onSelectedDurationChange(selectedDurationSeconds)
            },
            enabled = isMinusButtonEnabled,
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.size(24.dp),
        )

        Text(
            text = HumanReadable.duration(selectedDurationSeconds.seconds),
            style = MaterialTheme.typography.titleSmall,
        )

        NCButton(
            content = ButtonContent(icon = Res.drawable.add),
            colors = Color.Noise.two.secondaryContainerColors(),
            onClick = {
                selectedDurationSeconds = min(selectedDurationSeconds + 1, maxCalibrationDuration)
                onSelectedDurationChange(selectedDurationSeconds)
            },
            enabled = isPlusButtonEnabled,
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.size(24.dp)
        )
    }
}
