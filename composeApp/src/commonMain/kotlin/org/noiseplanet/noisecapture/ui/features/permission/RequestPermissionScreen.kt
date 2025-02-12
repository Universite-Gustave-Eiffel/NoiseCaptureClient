package org.noiseplanet.noisecapture.ui.features.permission

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton


/**
 * Presents required permissions to the user with controls to either request the
 * permission if it was not yet asked, or to open the corresponding settings page
 * if permission was already previously denied
 *
 * TODO: Instead of pushing this screen into the navigation stack, make it pop on top of
 *       the current screen when needed. Pressing the back button should also close the enclosing
 *       screen and pressing the next button should dismiss both screens. Maybe this can be done
 *       with a nested navigation controller?
 */
@Composable
fun RequestPermissionScreen(
    onClickNextButton: () -> Unit,
    viewModel: RequestPermissionScreenViewModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.White,
        modifier = modifier.fillMaxSize()
            .padding(all = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(viewModel.image),
                contentDescription = stringResource(viewModel.title),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(viewModel.title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(viewModel.description),
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                )
            }

            NCButton(
                onClick = viewModel::requestPermission,
                viewModel = viewModel.grantPermissionButtonViewModel
            )
        }
    }
}
