import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.toRoute
import kotlinx.browser.document
import kotlinx.browser.window
import org.noiseplanet.noisecapture.initKoin
import org.noiseplanet.noisecapture.platformModule
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute
import org.noiseplanet.noisecapture.ui.navigation.HomeRoute
import org.noiseplanet.noisecapture.ui.navigation.Route

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalBrowserHistoryApi::class,
    ExperimentalWasmJsInterop::class
)
fun main() {

    ComposeViewport(document.body!!) {

        // - DI

        initKoin(
            additionalModules = listOf(
                platformModule
            )
        )


        // - Navigation

        App(onNavHostReady = { navController ->
            // Get the manually entered route
            val initRouteId = window.location.hash.substringAfter('#', "")
            Route.fromUrlPath(initRouteId)?.let { route ->
                // Do nothing, app starts on home route anyway
                if (route is HomeRoute) return@let
                navController.navigate(route)
            }

            navController.bindToBrowserNavigation { entry ->
                val routeName = entry.destination.route.orEmpty()
                val path = when {
                    // Identifies the route using its serial descriptor.
                    // For route with custom parameter, we need to deserialize the corresponding
                    // route class in order to access route parameters.
                    routeName.startsWith(DetailsRoute.serializer().descriptor.serialName) -> {
                        entry.toRoute<DetailsRoute>().toUrlPath()
                    }

                    else -> {
                        // Otherwise, use default route path which only consists of route id.
                        entry.toRoute<Route>().toUrlPath()
                    }
                }
                // This string must always start with the `#` character to keep
                // the processing at the front end
                // See: https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html
                "#$path"
            }
        })
    }
}
