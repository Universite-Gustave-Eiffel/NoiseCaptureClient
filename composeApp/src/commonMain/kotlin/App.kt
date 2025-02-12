import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.ui.navigation.RootCoordinator
import org.noiseplanet.noisecapture.ui.navigation.RootCoordinatorViewModel

/**
 * Entry point of the Compose app.
 */
@Composable
@Preview
fun App() {
    KoinContext {
        MaterialTheme {
            val coordinatorViewModel: RootCoordinatorViewModel = koinInject()
            
            RootCoordinator(viewModel = coordinatorViewModel)
        }
    }
}
