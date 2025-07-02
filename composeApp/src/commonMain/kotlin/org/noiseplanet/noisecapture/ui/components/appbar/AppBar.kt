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

    val currentBackStackEntry by appBarState.navController.currentBackStackEntryAsState()
    val previousBackStackEntry = appBarState.navController.previousBackStackEntry
    val currentRoute = currentBackStackEntry?.destination?.route
    val canNavigateUp = currentRoute != null && previousBackStackEntry != null

    val actions by appBarState.actions.collectAsStateWithLifecycle()
    val title = appBarState.viewModel?.title


    // - Layout

    CenterAlignedTopAppBar(
        title = {
            title?.let {
                Text(stringResource(it))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onSecondary,
            containerColor = MaterialTheme.colorScheme.secondary,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
        ),
        navigationIcon = {
            if (canNavigateUp) {
                IconButton(onClick = {
                    appBarState.navController.navigateUp()
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
