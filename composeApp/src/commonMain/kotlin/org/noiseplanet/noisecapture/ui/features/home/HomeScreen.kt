package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.ui.features.home.menuitem.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.menuitem.MenuItem

/**
 * Home screen layout.
 *
 * TODO: Improve UI once more clearly defined
 */
@Composable
fun HomeScreen(
    navigationController: NavController,
    viewModel: HomeScreenViewModel = koinInject(),
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
            content = {
                items(viewModel.menuItems) { viewModel ->
                    MenuItem(
                        viewModel,
                        navigateTo = { route ->
                            navigationController.navigate(route.name)
                        },
                    )
                }
            }
        )
    }
}
