package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItem
import org.noiseplanet.noisecapture.util.AdaptiveUtil

@OptIn(ExperimentalFoundationApi::class, KoinExperimentalAPI::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
) {
    // - DI

    rememberKoinModules {
        listOf(settingsModule)
    }


    // - Properties

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()


    // - Lifecycle

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect {
                focusManager.clearFocus()
            }
    }


    // - Layout

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Box(contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = navBarBottomPadding + 16.dp,
                ),
                modifier = Modifier.widthIn(max = AdaptiveUtil.MAX_FULL_SCREEN_WIDTH)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                viewModel.settingsItems.forEach { (sectionTitle, sectionItems) ->
                    stickyHeader {
                        ListSectionHeader(
                            title = sectionTitle,
                            paddingTop = 16.dp,
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(start = 16.dp)
                        )
                    }

                    itemsIndexed(sectionItems) { index, viewModel ->
                        SettingsItem(
                            viewModel = viewModel,
                            isFirstInSection = index == 0,
                            isLastInSection = index == (sectionItems.size - 1),
                        )
                    }
                }
            }
        }
    }
}
