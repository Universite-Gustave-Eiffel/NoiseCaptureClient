package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItem
import org.noiseplanet.noisecapture.ui.theme.listBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect {
                focusManager.clearFocus()
            }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
        modifier = Modifier.background(listBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                focusManager.clearFocus()
            }
    ) {
        viewModel.settingsItems.forEach { (sectionTitle, sectionItems) ->
            stickyHeader {
                SettingsSectionHeader(sectionTitle)
            }

            items(sectionItems) { viewModel ->
                SettingsItem(viewModel)
            }
        }
    }
}
