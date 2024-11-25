package org.noiseplanet.noisecapture.ui.components.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.back_button
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.navigation.Route

@Composable
fun AppBar(
    appBarState: AppBarState,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by appBarState.navController.currentBackStackEntryAsState()
    val previousBackStackEntry = appBarState.navController.previousBackStackEntry
    val currentRoute = currentBackStackEntry?.destination?.route
    val canNavigateUp = currentRoute != null && previousBackStackEntry != null

    TopAppBar(
        title = {
            if (currentRoute != null) {
                val title = Route.valueOf(currentRoute).title
                Text(stringResource(title))
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                items(appBarState.actions) { action ->
                    IconButton(onClick = action.onClick) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = stringResource(action.iconContentDescription),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
