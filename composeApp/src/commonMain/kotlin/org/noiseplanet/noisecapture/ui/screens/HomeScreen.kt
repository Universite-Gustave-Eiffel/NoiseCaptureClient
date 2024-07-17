package org.noiseplanet.noisecapture.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.menu_about
import noisecapture.composeapp.generated.resources.menu_calibration
import noisecapture.composeapp.generated.resources.menu_feedback
import noisecapture.composeapp.generated.resources.menu_help
import noisecapture.composeapp.generated.resources.menu_history
import noisecapture.composeapp.generated.resources.menu_map
import noisecapture.composeapp.generated.resources.menu_new_measurement
import noisecapture.composeapp.generated.resources.menu_settings
import noisecapture.composeapp.generated.resources.menu_statistics
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.MenuItem

/**
 * Home screen layout.
 *
 * TODO: Improve UI once more clearly defined
 * TODO: Figure out a clean design pattern to handle click events (delegate?, pass down navigation controller?)
 */
@Composable
fun HomeScreen(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuItems = arrayOf(
        MenuItem(
            stringResource(Res.string.menu_new_measurement),
            Icons.Filled.Mic,
            onClick = onClick
        ),
        MenuItem(stringResource(Res.string.menu_history), Icons.Filled.History) {},
        MenuItem(stringResource(Res.string.menu_feedback), Icons.Filled.HistoryEdu) {},
        MenuItem(stringResource(Res.string.menu_statistics), Icons.Filled.Timeline) {},
        MenuItem(stringResource(Res.string.menu_map), Icons.Filled.Map) {},
        MenuItem(stringResource(Res.string.menu_help), Icons.AutoMirrored.Filled.Help) {},
        MenuItem(stringResource(Res.string.menu_about), Icons.Filled.Info) {},
        MenuItem(stringResource(Res.string.menu_calibration), Icons.Filled.CenterFocusWeak) {},
        MenuItem(stringResource(Res.string.menu_settings), Icons.Filled.Settings) {},
    )

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
                items(menuItems.size) { index ->
                    Button(
                        onClick = menuItems[index].onClick,
                        modifier = Modifier.aspectRatio(1f).padding(12.dp),
                    ) {
                        Icon(
                            imageVector = menuItems[index].imageVector,
                            menuItems[index].label,
                            modifier.fillMaxSize(),
                        )
                    }
                }
            }
        )
    }
}
