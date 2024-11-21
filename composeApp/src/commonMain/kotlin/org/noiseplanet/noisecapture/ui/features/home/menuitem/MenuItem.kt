package org.noiseplanet.noisecapture.ui.features.home.menuitem

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.navigation.Route

@Composable
fun MenuItem(
    viewModel: MenuItemViewModel,
    navigateTo: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = {
            viewModel.route?.let {
                navigateTo(it)
            }
        },
        modifier = Modifier.aspectRatio(1f).padding(12.dp),
    ) {
        Icon(
            imageVector = viewModel.imageVector,
            stringResource(viewModel.label),
            modifier.fillMaxSize(),
        )
    }
}
