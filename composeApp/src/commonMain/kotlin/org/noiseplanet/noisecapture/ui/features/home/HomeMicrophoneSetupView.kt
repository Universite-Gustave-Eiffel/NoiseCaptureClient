package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_mic_not_calibrated
import noisecapture.composeapp.generated.resources.home_mic_setup_section_header
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.audio.mic.MicrophoneInfo
import org.noiseplanet.noisecapture.audio.mic.MicrophoneType
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun HomeMicrophoneSetupView(
    modifier: Modifier = Modifier,
) {

    // - Properties

    val viewModel: HomeMicrophoneSetupViewModel = koinViewModel()
    val availableDevices by viewModel.availableDevices.collectAsStateWithLifecycle()
    val activeDevice by viewModel.activeDevice.collectAsStateWithLifecycle()

    var showMicrophoneSelectMenu by remember { mutableStateOf(false) }


    // - Layout

    // TODO: If permission isn't granted, show a placeholder message

    Column(modifier = modifier) {
        ListSectionHeader(
            title = Res.string.home_mic_setup_section_header,
            modifier = Modifier.padding(start = 12.dp),
        )

        Column {
            Box(
                modifier = Modifier
                    .clip(
                        MaterialTheme.shapes.large.copy(
                            bottomStart = ZeroCornerSize,
                            bottomEnd = ZeroCornerSize,
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { showMicrophoneSelectMenu = true }
            ) {
                activeDevice?.getTitleAndDescription()?.let { (title, description) ->
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                MicrophoneSelectMenu(
                    expanded = showMicrophoneSelectMenu,
                    onDismissRequest = { showMicrophoneSelectMenu = false },
                    onSelectMicrophone = {
                        viewModel.selectMicrophone(it)
                        showMicrophoneSelectMenu = false
                    },
                    items = availableDevices,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
                    .background(
                        color = NoiseLevelColorRamp.level6Light,
                        shape = MaterialTheme.shapes.large.copy(
                            topStart = ZeroCornerSize,
                            topEnd = ZeroCornerSize
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.home_mic_not_calibrated),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoiseLevelColorRamp.level6Dark,
                    modifier = Modifier.weight(1f),
                )

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NoiseLevelColorRamp.level6Dark,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NCButton(
                        viewModel = viewModel.calibrationButtonViewModel,
                        onClick = {}
                    )
                }
            }
        }
    }
}


@Composable
private fun MicrophoneSelectMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectMicrophone: (MicrophoneInfo) -> Unit,
    items: List<MicrophoneInfo>,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        for (item in items) {
            DropdownMenuItem(
                text = {
                    val (title, description) = item.getTitleAndDescription()

                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                onClick = {
                    onSelectMicrophone(item)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}


@Composable
private fun MicrophoneInfo.getTitleAndDescription(): Pair<String, String> {
    // If device type is unknown, display device name as title instead
    return if (type != MicrophoneType.UNKNOWN) {
        Pair(stringResource(type.displayName), label)
    } else {
        Pair(label, stringResource(type.displayName))
    }
}
