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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.operation.push
import com.bumble.appyx.navigation.modality.NodeContext
import com.bumble.appyx.navigation.node.LeafNode
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.ui.theme.*

class HomeScreen(
    nodeContext: NodeContext,
    private val backStack: BackStack<ScreenData>
) : LeafNode(nodeContext) {

    @Composable
    override fun Content(modifier: Modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val menuItems = arrayOf(
                MenuItem("Test label", Mic,
                    onClick = { backStack.push(ScreenData.PermissionTarget) }),
                MenuItem("Measurement History", overview(),
                    onClick = {}),
                MenuItem("Measurement feedback", clinicalNotes(),
                    onClick = {}),
                MenuItem("Measurement statistics", ShowChart,
                    onClick = {}),
                MenuItem("Last measurement map", Map,
                    onClick = {}),
                MenuItem("Help", Help,
                    onClick = {}),
                MenuItem("About", Info,
                    onClick = {}),
                MenuItem("Calibration", CenterFocusWeak,
                    onClick = {}),
                MenuItem("Settings", Settings,
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
