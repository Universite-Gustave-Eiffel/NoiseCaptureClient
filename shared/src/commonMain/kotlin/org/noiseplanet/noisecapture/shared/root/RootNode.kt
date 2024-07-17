package org.noiseplanet.noisecapture.shared.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.BackStackModel
import com.bumble.appyx.components.backstack.operation.push
import com.bumble.appyx.components.backstack.ui.parallax.BackStackParallax
import com.bumble.appyx.navigation.composable.AppyxNavigationContainer
import com.bumble.appyx.navigation.modality.NodeContext
import com.bumble.appyx.navigation.node.Node
import com.bumble.appyx.navigation.node.LeafNode
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.noiseplanet.noisecapture.shared.ScreenData
import org.noiseplanet.noisecapture.shared.ScreenData.HomeTarget
import org.noiseplanet.noisecapture.shared.ScreenData.PermissionTarget
import org.noiseplanet.noisecapture.shared.child.HomeScreen
import org.noiseplanet.noisecapture.shared.child.MeasurementScreen
import org.noiseplanet.noisecapture.shared.child.NavigationScreen
import org.noiseplanet.noisecapture.shared.child.PermissionScreen

class RootNode(
    nodeContext: NodeContext,
    private val backStack: BackStack<ScreenData> = BackStack(
        model = BackStackModel(
            initialTargets = listOf(HomeTarget),
            savedStateMap = nodeContext.savedStateMap
        ),
        visualisation = { BackStackParallax(it) }
    ),
    val koin: Koin
) : Node<ScreenData>(
    nodeContext = nodeContext,
    appyxComponent = backStack
) {

    @OptIn(KoinInternalApi::class)
    override fun buildChildNode(navTarget: ScreenData, nodeContext: NodeContext): LeafNode =
        when (navTarget) {
            is PermissionTarget -> PermissionScreen(
                nodeContext,
                koin.get(),
                ::onPermissionGrantedBeforeMeasurement
            )

            is HomeTarget -> HomeScreen(
                nodeContext,
                backStack
            )

            is ScreenData.MeasurementTarget -> MeasurementScreen(
                nodeContext,
                backStack,
                koin.get(),
                koin.logger
            )
        }

    private fun onPermissionGrantedBeforeMeasurement() {
        backStack.push(ScreenData.MeasurementTarget)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            NavigationScreen(backStack) {
                AppyxNavigationContainer(
                    appyxComponent = backStack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
