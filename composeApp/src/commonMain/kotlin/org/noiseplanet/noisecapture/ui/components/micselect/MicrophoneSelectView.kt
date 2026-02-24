package org.noiseplanet.noisecapture.ui.components.micselect

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.audio.mic.MicrophoneInfo
import org.noiseplanet.noisecapture.audio.mic.MicrophoneType
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun MicrophoneSelectView(
    viewModel: MicrophoneSelectViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    // - Properties

    val availableDevices by viewModel.availableDevices.collectAsStateWithLifecycle()
    val activeDevice by viewModel.activeDevice.collectAsStateWithLifecycle()

    var showMicrophoneSelectMenu by remember { mutableStateOf(false) }


    // - Layout

    Box(
        modifier = modifier.clickable { showMicrophoneSelectMenu = true }
    ) {
        activeDevice?.getTitleAndDescription()?.let { (title, description) ->
            Column(modifier = Modifier.padding(contentPadding)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
        containerColor = NoiseLevelColorRamp.level1Light,
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
