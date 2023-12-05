package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Battery4Bar
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.operation.push
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import org.noise_planet.noisecapture.shared.Screens
import org.noise_planet.noisecapture.shared.ui.theme.rememberClinicalNotes
import org.noise_planet.noisecapture.shared.ui.theme.rememberCalibrate

class HomeScreen(buildContext: BuildContext,
                 val backStack: BackStack<Screens>) : Node(buildContext) {
    @Composable
    override fun View(modifier: Modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val menuItems = arrayOf(
                MenuItem("Test label", Icons.Outlined.Mic,
                    onClick = { backStack.push(Screens.PermissionTarget) }),
                MenuItem("Measurement History", Icons.Outlined.FormatListBulleted,
                    onClick = {}),
                MenuItem("Measurement feedback", rememberClinicalNotes(),
                    onClick = {}),
                MenuItem("Measurement statistics", Icons.Outlined.ShowChart,
                    onClick = {}),
                MenuItem("Last measurement map", Icons.Outlined.Map,
                    onClick = {}),
                MenuItem("Help", Icons.Outlined.Help,
                    onClick = {}),
                MenuItem("About", Icons.Outlined.Info,
                    onClick = {}),
                MenuItem("Calibration", rememberCalibrate(),
                    onClick = {}),
                MenuItem("Settings", Icons.Outlined.Info,
                    onClick = {})
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ),
                content = {
                items(menuItems.size) {
                    index ->
                        Button(
                            onClick = menuItems[index].onClick,
                            modifier = Modifier.aspectRatio(1f).padding(12.dp)
                        ) {
                            Icon(
                                imageVector = menuItems[index].imageVector,
                                menuItems[index].label, modifier.fillMaxSize()
                            )
                        }

                }
            })
        }
    }
}

data class MenuItem(val label:String, val imageVector: ImageVector, val onClick: () -> Unit)
