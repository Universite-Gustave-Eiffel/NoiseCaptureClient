package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter

/**
 * Home screen layout.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    router: HomeRouter,
    showPermissionPrompt: (Permission) -> Unit,
) {
    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(homeModule)
    }


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 32.dp)
        ) {
            SoundLevelMeterHeaderView(
                viewModel = viewModel,
                onClickOpenSoundLevelMeterButton = router::onClickOpenSoundLevelMeterButton,
                showPermissionPrompt = showPermissionPrompt,
            )

            HomeMapView(
                router = router,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            LastMeasurementsView(
                onClickMeasurement = router::onClickMeasurement,
                onClickOpenHistoryButton = router::onClickOpenHistoryButton,
            )

            // TODO: Add device calibration section

            // TODO: Add more info section
        }
    }
}
