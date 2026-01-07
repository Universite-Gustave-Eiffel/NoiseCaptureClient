package org.noiseplanet.noisecapture.ui.features.settings.item

import Platform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.settings_info_app_device
import noisecapture.composeapp.generated.resources.settings_info_app_platform
import noisecapture.composeapp.generated.resources.settings_info_app_title
import noisecapture.composeapp.generated.resources.settings_info_app_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.util.clipEntry

@Composable
fun SettingsInfoItem(
    modifier: Modifier = Modifier,
) {
    // - Properties

    val userAgent = koinInject<Platform>().userAgent

    val coroutineScope = rememberCoroutineScope()
    val clipboard: Clipboard = LocalClipboard.current

    val version = "${userAgent.versionName} (${userAgent.versionCode})"
    val platform = "${userAgent.osName} - ${userAgent.osVersion}"
    val device =
        "${userAgent.deviceManufacturer} ${userAgent.deviceModelName} (${userAgent.deviceModelCode})"
    val info = """
        ${stringResource(Res.string.settings_info_app_version)} $version
        ${stringResource(Res.string.settings_info_app_platform)} $platform
        ${stringResource(Res.string.settings_info_app_device)} $device
    """.trimIndent()


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ).padding(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                stringResource(Res.string.settings_info_app_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = info,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick = {
                coroutineScope.launch {
                    clipboard.setClipEntry(clipEntry(info))
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}