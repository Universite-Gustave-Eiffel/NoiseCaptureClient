package org.noiseplanet.noisecapture.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItem
import org.noiseplanet.noisecapture.ui.theme.listBackground

@Composable
fun SettingsScreen(
    navigationController: NavController,
    viewModel: SettingsScreenViewModel = koinInject(),
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.background(listBackground)
    ) {
        items(viewModel.settingsItems) { viewModel ->
            SettingsItem(viewModel) { route ->
                navigationController.navigate(route.name)
            }
        }
    }
}
