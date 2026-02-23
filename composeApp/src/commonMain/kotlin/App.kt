import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.ui.navigation.RootCoordinator
import org.noiseplanet.noisecapture.ui.navigation.RootCoordinatorViewModel
import org.noiseplanet.noisecapture.ui.theme.AppTheme

/**
 * Entry point of the Compose app.
 *
 * @param navController Root navigation controller. Can be overridden for platform specific customisation.
 */
@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {},
) {
    AppTheme {
        val coordinatorViewModel: RootCoordinatorViewModel = koinInject()

        RootCoordinator(
            viewModel = coordinatorViewModel,
            onNavHostReady = onNavHostReady,
        )
    }
}
