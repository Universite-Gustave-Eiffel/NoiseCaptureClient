package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItem
import org.noiseplanet.noisecapture.ui.theme.listBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    navigationController: NavController,
    viewModel: SettingsScreenViewModel = koinInject(),
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.background(listBackground)
    ) {
        viewModel.settingsItems.forEach { (sectionTitle, sectionItems) ->
            stickyHeader {
                Text(stringResource(sectionTitle))
            }

            items(sectionItems) { viewModel ->
                SettingsItem(viewModel)
            }
        }
    }
}
