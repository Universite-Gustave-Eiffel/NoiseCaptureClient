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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_start_recording_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.theme.NotoSansMono
import org.noiseplanet.noisecapture.util.shadow.dropShadow

/**
 * Start/Stop and Play/Pause buttons to manage current recording
 *
 * @param onMeasurementDone Called when measurement recording ends, with UUID as parameter.
 */
@Composable
fun RecordingControls(
    onMeasurementDone: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val viewModel: RecordingControlsViewModel = koinViewModel()

    val isRecording by viewModel.isRecordingFlow.collectAsStateWithLifecycle()
    val isAudioSourceRunning by viewModel.isAudioSourceRunningFlow.collectAsStateWithLifecycle()


    // - Lifecycle

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.registerListener(onMeasurementDone)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.deregisterListener()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


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
                    .dropShadow(shape = CircleShape)
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
                            onClick = viewModel::toggleAudioSource,
                            modifier = Modifier.size(50.dp),
                        ) {
                            AnimatedContent(
                                targetState = isAudioSourceRunning,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                }
                            ) { isAudioSourceRunning ->
                                Icon(
                                    imageVector = if (isAudioSourceRunning) {
                                        Icons.Filled.Pause
                                    } else {
                                        Icons.Filled.PlayArrow
                                    },
                                    contentDescription = "Pause",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(64.dp))

                        Text(
                            text = "00:12:30",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.NotoSansMono,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
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
                        onClick = viewModel::toggleRecording,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
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
                    onClick = viewModel::toggleRecording,
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    modifier = Modifier.padding(start = (50 + 8).dp) // Place stop button after pause button
                        .dropShadow(shape = CircleShape)
                        .size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "End recording",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
