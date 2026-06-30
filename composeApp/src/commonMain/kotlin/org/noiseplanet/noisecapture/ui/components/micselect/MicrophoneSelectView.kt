package org.noiseplanet.noisecapture.ui.components.micselect

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.arrow_drop_down
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.model.dao.MicrophoneType
import org.noiseplanet.noisecapture.ui.components.NCDropdownMenu
import org.noiseplanet.noisecapture.ui.components.NCDropdownMenuItem
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Neutral


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
                        painter = painterResource(Res.drawable.arrow_drop_down),
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

        NCDropdownMenu(
            expanded = showMicrophoneSelectMenu,
            onDismissRequest = { showMicrophoneSelectMenu = false },
            colors = Color.Neutral.secondaryContainerColors(),
            modifier = modifier,
        ) {
            availableDevices.forEach { device ->
                val (title, description) = device.getTitleAndDescription()

                NCDropdownMenuItem(
                    label = title,
                    supportingText = description,
                    onClick = {
                        viewModel.selectMicrophone(device)
                        showMicrophoneSelectMenu = false
                    }
                )
            }
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
