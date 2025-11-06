package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter

/**
 * Home screen layout.
 */
@OptIn(KoinExperimentalAPI::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    router: HomeRouter,
) {
    // - DI

    rememberKoinModules {
        listOf(homeModule)
    }


    // - Properties

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        when (sizeClass.minWidthDp) {
            WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> HomeScreenLarge(viewModel, router)
            else -> HomeScreenCompact(viewModel, router)
        }
    }
}


@Composable
private fun HomeScreenCompact(viewModel: HomeScreenViewModel, router: HomeRouter) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 32.dp)
    ) {
        SoundLevelMeterHeaderView(
            viewModel = viewModel,
            onClickOpenSoundLevelMeterButton = router::onClickOpenSoundLevelMeterButton,
            showPermissionPrompt = router.showPermissionPrompt,
        )

        HomeMapView(
            router = router,
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(1.5f)
                .padding(horizontal = 16.dp)
        )

        LastMeasurementsView(
            onClickMeasurement = router::onClickMeasurement,
            onClickOpenHistoryButton = router::onClickOpenHistoryButton,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // TODO: Add device calibration section

        // TODO: Add more info section
    }
}


@Composable
private fun HomeScreenLarge(viewModel: HomeScreenViewModel, router: HomeRouter) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier//.verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            SoundLevelMeterHeaderView(
                viewModel = viewModel,
                onClickOpenSoundLevelMeterButton = router::onClickOpenSoundLevelMeterButton,
                showPermissionPrompt = router.showPermissionPrompt,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )

            LastMeasurementsView(
                onClickMeasurement = router::onClickMeasurement,
                onClickOpenHistoryButton = router::onClickOpenHistoryButton,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            HomeMapView(
                router = router,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            Box(modifier = Modifier.weight(1f))
        }

        // TODO: Add device calibration section

        // TODO: Add more info section
    }
}
