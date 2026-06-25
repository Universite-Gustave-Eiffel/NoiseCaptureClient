package org.noiseplanet.noisecapture.ui.components.audioplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.delete
import noisecapture.composeapp.generated.resources.details_audio_player_description
import noisecapture.composeapp.generated.resources.details_audio_player_disclaimer
import noisecapture.composeapp.generated.resources.details_audio_player_title
import noisecapture.composeapp.generated.resources.details_delete_measurement_audio_dialog_text
import noisecapture.composeapp.generated.resources.details_delete_measurement_dialog_title
import noisecapture.composeapp.generated.resources.pause
import noisecapture.composeapp.generated.resources.play_arrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.features.details.manage.DeleteConfirmationDialog
import org.noiseplanet.noisecapture.ui.features.details.manage.DeleteConfirmationDialogViewModel
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.util.toHhMmSs
import kotlin.time.Duration


@Composable
fun AudioPlayerView(
    measurement: Measurement,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val viewModel: AudioPlayerViewModel = koinViewModel {
        parametersOf(measurement)
    }

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()

    val playerCurrentPosition by viewModel.currentPosition
        .collectAsStateWithLifecycle(Duration.ZERO)

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }


    // - Lifecycle

    if (!isReady) {
        return
    }


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(Res.string.details_audio_player_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.details_audio_player_description) + " ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    // show disclaimer with bold font
                    append(stringResource(Res.string.details_audio_player_disclaimer))
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            NCButton(
                onClick = { viewModel.togglePlayPause() },
                content = ButtonContent(icon = if (isPlaying) Res.drawable.pause else Res.drawable.play_arrow),
                colors = Color.Noise.two.secondaryContainerColors(),
                modifier = Modifier.size(32.dp)
            )
            AudioPlayerSlider(viewModel)

            Text(
                // Display remaining time
                text = "-" + (playerCurrentPosition - viewModel.duration)
                    .toHhMmSs(hideHoursIfZero = true),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(
                onClick = { showDeleteConfirmationDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Delete audio"
                )
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            viewModel = DeleteConfirmationDialogViewModel(
                title = Res.string.details_delete_measurement_dialog_title,
                text = Res.string.details_delete_measurement_audio_dialog_text,
                onDismissRequest = { showDeleteConfirmationDialog = false },
                onConfirm = {
                    viewModel.deleteAudioClip()
                    showDeleteConfirmationDialog = false
                }
            ))
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
        colors = SliderDefaults.colors(
            thumbColor = Color.Noise.two.dark,
            activeTickColor = Color.Noise.two.dark,
            activeTrackColor = Color.Noise.two.dark,
            inactiveTickColor = Color.Noise.two.light,
            inactiveTrackColor = Color.Noise.two.light,
        ),
        modifier = Modifier.weight(1f),
    )
}
