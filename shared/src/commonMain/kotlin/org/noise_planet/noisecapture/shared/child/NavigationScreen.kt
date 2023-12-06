package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.activeElement
import com.bumble.appyx.components.backstack.activeInteractionTarget
import com.bumble.appyx.components.backstack.operation.pop
import org.noise_planet.noisecapture.shared.Screens

@Composable
fun NavigationScreen(
    backStack: BackStack<Screens>,
    body: @Composable (PaddingValues) -> Unit
) {
    val canBackPress by backStack.canHandeBackPress().collectAsState(false)
    val currentTitle by backStack.model.output.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentTitle.currentTargetState.active.interactionTarget.title) },
                navigationIcon = {
                    IconButton(onClick = {backStack.pop()}, enabled = canBackPress) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        content = body
    )
}
