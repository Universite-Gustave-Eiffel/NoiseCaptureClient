package org.noise_planet.noisecapture.shared.child.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.adrianwitaszak.kmmpermissions.permissions.service.PermissionsService
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.BackStackModel
import com.bumble.appyx.components.backstack.activeElement
import com.bumble.appyx.components.backstack.operation.pop
import com.bumble.appyx.components.backstack.operation.push
import com.bumble.appyx.components.backstack.ui.parallax.BackStackParallax
import com.bumble.appyx.navigation.composable.AppyxComponent
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import com.bumble.appyx.navigation.node.ParentNode
import org.noise_planet.noisecapture.shared.child.ChildNode1
import org.noise_planet.noisecapture.shared.child.root.RootNode.InteractionTarget
import org.noise_planet.noisecapture.shared.child.root.RootNode.InteractionTarget.Child1
import com.bumble.appyx.utils.multiplatform.Parcelable
import com.bumble.appyx.utils.multiplatform.Parcelize
import org.koin.core.Koin

class RootNode(
    buildContext: BuildContext,
    private val backStack: BackStack<InteractionTarget> = BackStack(
        model = BackStackModel(
            initialTargets = listOf(Child1),
            savedStateMap = buildContext.savedStateMap
        ),
        visualisation = { BackStackParallax(it) }
    ),
    val koin: Koin
) : ParentNode<InteractionTarget>(
    buildContext = buildContext,
    appyxComponent = backStack
) {

    sealed class InteractionTarget : Parcelable {
        @Parcelize
        data object Child1 : InteractionTarget()
    }

    override fun resolve(interactionTarget: InteractionTarget, buildContext: BuildContext): Node =
        ChildNode1(buildContext, koin.get<PermissionsService>())

    @Composable
    override fun View(modifier: Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            AppyxComponent(
                appyxComponent = backStack,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
