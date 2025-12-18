package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.details_audio_player_description
import noisecapture.composeapp.generated.resources.details_audio_player_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.util.throttleLatest
import org.noiseplanet.noisecapture.util.toHhMmSs
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

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(Res.string.details_audio_player_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = stringResource(Res.string.details_audio_player_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp),
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
                    .toHhMmSs(hideHoursIfZero = true),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
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
