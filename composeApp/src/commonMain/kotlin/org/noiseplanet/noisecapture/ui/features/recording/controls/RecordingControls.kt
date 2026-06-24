package org.noiseplanet.noisecapture.ui.features.recording.controls

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_start_recording_button_title
import noisecapture.composeapp.generated.resources.mic
import noisecapture.composeapp.generated.resources.pause
import noisecapture.composeapp.generated.resources.play_arrow
import noisecapture.composeapp.generated.resources.stop
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.theme.titleMono
import org.noiseplanet.noisecapture.util.ncDropShadow
import org.noiseplanet.noisecapture.util.toHhMmSs

/**
 * Start/Stop and Play/Pause buttons to manage current recording
 */
@Composable
fun RecordingControls(
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: RecordingControlsViewModel = koinViewModel()

    val isRecording by viewModel.isRecordingFlow.collectAsStateWithLifecycle()
    val isAudioSourceRunning by viewModel.isAudioSourceRunningFlow.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDurationFlow.collectAsStateWithLifecycle()


    // - Layout

    Box(
        // Make box fill max width and center its content
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 7.dp)
                    .height(50.dp)
                    .ncDropShadow(shape = CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
                    .animateContentSize()
            ) {
                AnimatedVisibility(
                    visible = isRecording,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = viewModel::togglePauseResume,
                            modifier = Modifier.size(50.dp),
                        ) {
                            AnimatedContent(
                                targetState = isAudioSourceRunning,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                }
                            ) { isAudioSourceRunning ->
                                Icon(
                                    painter = painterResource(
                                        if (isAudioSourceRunning) {
                                            Res.drawable.pause
                                        } else {
                                            Res.drawable.play_arrow
                                        }
                                    ),
                                    contentDescription = "Pause",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(64.dp))

                        Text(
                            text = recordingDuration.toHhMmSs(),
                            style = MaterialTheme.typography.titleMono,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isRecording,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Button(
                        colors = ButtonDefaults.textButtonColors(),
                        onClick = viewModel::startRecording,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.mic),
                            contentDescription = "Start recording",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.measurement_start_recording_button_title),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isRecording,
                enter = scaleIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = onStopRecording,
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    modifier = Modifier.padding(start = (50 + 8).dp) // Place stop button after pause button
                        .ncDropShadow(shape = CircleShape)
                        .size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.stop),
                        contentDescription = "End recording",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
