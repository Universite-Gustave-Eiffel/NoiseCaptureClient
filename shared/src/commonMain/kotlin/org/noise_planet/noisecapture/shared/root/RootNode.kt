package org.noise_planet.noisecapture.shared.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adrianwitaszak.kmmpermissions.permissions.service.PermissionsService
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.BackStackModel
import com.bumble.appyx.components.backstack.operation.pop
import com.bumble.appyx.components.backstack.ui.parallax.BackStackParallax
import com.bumble.appyx.navigation.composable.AppyxComponent
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import com.bumble.appyx.navigation.node.ParentNode
import org.noise_planet.noisecapture.shared.child.PermissionScreen
import org.noise_planet.noisecapture.shared.Screens
import org.noise_planet.noisecapture.shared.Screens.PermissionTarget
import org.noise_planet.noisecapture.shared.Screens.HomeTarget
import org.koin.core.Koin
import org.noise_planet.noisecapture.shared.child.HomeScreen
import org.noise_planet.noisecapture.shared.child.NavigationScreen

class RootNode(
    buildContext: BuildContext,
    private val backStack: BackStack<Screens> = BackStack(
        model = BackStackModel(
            initialTargets = listOf(HomeTarget),
            savedStateMap = buildContext.savedStateMap
        ),
        visualisation = { BackStackParallax(it) }
    ),
    val koin: Koin
) : ParentNode<Screens>(
    buildContext = buildContext,
    appyxComponent = backStack
) {

    override fun resolve(interactionTarget: Screens, buildContext: BuildContext): Node =
        when(interactionTarget) {
            is PermissionTarget -> PermissionScreen(buildContext, koin.get())
            is HomeTarget -> HomeScreen(buildContext, backStack)
        }

    @Composable
    override fun View(modifier: Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            NavigationScreen(backStack) {
                AppyxComponent(
                    appyxComponent = backStack,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}
