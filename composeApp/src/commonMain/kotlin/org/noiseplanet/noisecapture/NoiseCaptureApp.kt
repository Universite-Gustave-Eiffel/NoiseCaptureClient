package org.noiseplanet.noisecapture

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.noiseplanet.noisecapture.ui.AppBar
import org.noiseplanet.noisecapture.ui.NavigationRoute
import org.noiseplanet.noisecapture.ui.screens.HomeScreen
import org.noiseplanet.noisecapture.ui.screens.PlatformInfoScreen


@Composable
fun NoiseCaptureApp(
    navController: NavHostController = rememberNavController(),
) {
    // Get current navigation back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = NavigationRoute.valueOf(
        backStackEntry?.destination?.route ?: NavigationRoute.Home.name
    )

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = NavigationRoute.Home.name,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = NavigationRoute.Home.name) {
                HomeScreen(
                    onClick = { navController.navigate(NavigationRoute.PlatformInfo.name) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
            composable(route = NavigationRoute.PlatformInfo.name) {
                PlatformInfoScreen(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}
