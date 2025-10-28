package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItem

@OptIn(ExperimentalFoundationApi::class, KoinExperimentalAPI::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
) {
    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(settingsModule)
    }


    // - Properties

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }


    // - Lifecycle

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect {
                focusManager.clearFocus()
            }
    }


    // - Layout

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                focusManager.clearFocus()
            }
        ) {
            viewModel.settingsItems.forEach { (sectionTitle, sectionItems) ->
                stickyHeader {
                    ListSectionHeader(
                        sectionTitle,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(start = 16.dp)
                    )
                }

                items(sectionItems) { viewModel ->
                    SettingsItem(viewModel)
                }
            }
        }
    }
}
