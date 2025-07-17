package org.noiseplanet.noisecapture.ui.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.noiseplanet.noisecapture.ui.components.appbar.AppBar
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarState
import org.noiseplanet.noisecapture.ui.components.appbar.rememberAppBarState
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionModal


/**
 * Root component of the app.
 * Currently handles the navigation stack, and navigation bar management.
 */
@Composable
fun RootCoordinator(
    viewModel: RootCoordinatorViewModel,
) {

    // - Properties

    val navController: NavHostController = rememberNavController()
    val appBarState: AppBarState = rememberAppBarState(navController)

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    val permissionPrompt: RootCoordinatorViewModel.PermissionPrompt? by viewModel
        .permissionPrompt.collectAsStateWithLifecycle()


    // - Lifecycle

    navController.addOnDestinationChangedListener { navController, _, _ ->
        // Triggered when navigating to a new screen or from another screen
        navController.currentBackStackEntry?.toRoute<Route>()?.let {
            viewModel.setCurrentRoute(it)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    // When the app is launched, setup audio source.
                    viewModel.setupAudioSource()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    // When app is sent to background, pause audio source if not recording
                    viewModel.stopAudioSourceIfNotRecording()
                }

                Lifecycle.Event.ON_RESUME -> {
                    // When app comes back to foreground and the current screen uses incoming
                    // audio, resume audio source if not recording
                    navController.currentBackStackEntry?.toRoute<Route>()?.let {
                        viewModel.setCurrentRoute(it)
                    }
                    // Refresh permission states in case user changed something in the settings
                    viewModel.refreshPermissionStates()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            if (viewModel.isRecording) {
                // If a recording is currently ongoing when the app stops, end recording to ensure
                // that results are saved properly.
                viewModel.endRecording()
            }
            viewModel.releaseAudioSource()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Navigation

    Scaffold(
        topBar = {
            AppBar(appBarState)
        }
    ) { innerPadding ->

        // Manages navigating between screens
        NavigationManager(
            navController = navController,
            appBarState = appBarState,
            innerPadding = innerPadding,
        )

        // If needed, prompt permission request to the user
        permissionPrompt?.let {
            RequestPermissionModal(
                permission = it.permission,
                isRequired = it.isRequired,
            )
        }
    }
}
