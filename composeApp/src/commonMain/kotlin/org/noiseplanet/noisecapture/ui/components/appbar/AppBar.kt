package org.noiseplanet.noisecapture.ui.components.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.back_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppBar(
    appBarState: AppBarState,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val screenViewModel = appBarState.viewModel ?: return
    val actions by screenViewModel.actions.collectAsStateWithLifecycle()

    val currentBackStackEntry by appBarState.navController.currentBackStackEntryAsState()
    val previousBackStackEntry = appBarState.navController.previousBackStackEntry
    val currentRoute = currentBackStackEntry?.destination?.route
    val canNavigateUp = currentRoute != null && previousBackStackEntry != null


    // - Layout

    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(screenViewModel.title))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        navigationIcon = {
            if (canNavigateUp) {
                IconButton(onClick = {
                    if (screenViewModel.confirmPopBackStack()) {
                        appBarState.navController.navigateUp()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back_button)
                    )
                }
            }
        },
        actions = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End)
            ) {
                items(actions) { action ->
                    IconButton(onClick = action.onClick) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.iconContentDescription?.let {
                                stringResource(it)
                            },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
