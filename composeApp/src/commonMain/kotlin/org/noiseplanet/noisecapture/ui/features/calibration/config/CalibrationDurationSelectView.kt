package org.noiseplanet.noisecapture.ui.features.calibration.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import nl.jacobras.humanreadable.HumanReadable
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds


@Composable
fun CalibrationDurationSelectView(
    onSelectedDurationChange: (durationSeconds: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val minCalibrationDuration: Int = 5
    val maxCalibrationDuration: Int = 30

    var selectedDurationSeconds: Int by remember { mutableStateOf(10) }
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
        FilledTonalButton(
            onClick = {
                selectedDurationSeconds = max(selectedDurationSeconds - 1, minCalibrationDuration)
                onSelectedDurationChange(selectedDurationSeconds)
            },
            enabled = isMinusButtonEnabled,
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "-",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Text(
            text = HumanReadable.duration(selectedDurationSeconds.seconds),
            style = MaterialTheme.typography.titleSmall,
        )

        FilledTonalButton(
            onClick = {
                selectedDurationSeconds = min(selectedDurationSeconds + 1, maxCalibrationDuration)
                onSelectedDurationChange(selectedDurationSeconds)
            },
            enabled = isPlusButtonEnabled,
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "+",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
