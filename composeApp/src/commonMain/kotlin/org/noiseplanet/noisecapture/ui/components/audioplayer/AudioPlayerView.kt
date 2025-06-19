package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Composable
fun AudioPlayerView(
    viewModel: AudioPlayerViewModel,
) {

    // - Properties

    val currentPosition by viewModel.currentPosition.collectAsState()
    val buttonViewModel by viewModel.playPauseButtonViewModel.collectAsState()


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        NCButton(
            onClick = { viewModel.togglePlayPause() },
            viewModel = buttonViewModel,
            modifier = Modifier.size(32.dp)
        )

        Slider(
            value = currentPosition.inWholeMilliseconds.toFloat(),
            valueRange = 0f..viewModel.duration.inWholeMilliseconds.toFloat(),
            onValueChange = { position ->
                val newPosition = position.toLong().toDuration(unit = DurationUnit.MILLISECONDS)
                viewModel.seek(newPosition)
            },
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "00:00",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
