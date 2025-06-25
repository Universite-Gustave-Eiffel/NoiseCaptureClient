package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.util.throttleLatest
import kotlin.math.abs
import kotlin.time.Duration


@Composable
fun AudioPlayerView(
    audioUrl: String,
) {

    // - Properties

    val viewModel: AudioPlayerViewModel = koinViewModel {
        parametersOf(audioUrl)
    }

    val buttonViewModel by viewModel.playPauseButtonViewModel.collectAsStateWithLifecycle()
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()

    val playerCurrentPosition by viewModel.currentPosition.throttleLatest(1_000)
        .collectAsStateWithLifecycle(Duration.ZERO)


    // - Lifecycle

    if (!isReady) {
        return
    }


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
        AudioPlayerSlider(viewModel)

        Text(
            // Display remaining time
            text = "-" + (playerCurrentPosition - viewModel.duration)
                .toComponents { hours, minutes, seconds, _ ->
                    val minutesString = abs(minutes).toString().padStart(2, '0')
                    val secondsString = abs(seconds).toString().padStart(2, '0')

                    if (hours > 0) {
                        // Only show hours count when recording is more than 1h long
                        val hoursString = abs(hours).toString().padStart(2, '0')
                        "$hoursString:$minutesString:$secondsString"
                    } else {
                        "$minutesString:$secondsString"
                    }
                },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}


@Composable
private fun RowScope.AudioPlayerSlider(
    viewModel: AudioPlayerViewModel,
) {
    // - Properties

    val lifecycleOwner = LocalLifecycleOwner.current
    val playerCurrentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    var seekPosition: Float? by remember { mutableStateOf(null) }


    // - Lifecycle

    DisposableEffect(lifecycleOwner) {
        // When view is destroyed, free up loaded audio
        onDispose {
            viewModel.release()
        }
    }


    // - Layout

    Slider(
        // If user is moving the play position, don't change the slider value
        // until they're done.
        value = seekPosition ?: (playerCurrentPosition / viewModel.duration).toFloat(),
        // When starting seeking, update seek position but don't change the play head
        // position until user releases the cursor.
        onValueChange = { position ->
            seekPosition = position
        },
        // When seeking ends, update play head position and resume to following
        // position updates from audio player.
        onValueChangeFinished = {
            seekPosition?.let {
                val newPosition = viewModel.duration * it.toDouble()
                viewModel.seek(newPosition)
            }
            seekPosition = null
        },
        modifier = Modifier.weight(1f),
    )
}
