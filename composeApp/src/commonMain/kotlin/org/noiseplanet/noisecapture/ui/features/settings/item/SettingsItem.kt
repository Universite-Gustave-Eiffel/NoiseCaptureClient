package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.util.IterableEnum

private const val CORNER_RADIUS: Float = 10f

@Composable
fun <T : Any> SettingsItem(
    viewModel: SettingsItemViewModel<T>,
) {
    val shape = RoundedCornerShape(
        topStart = if (viewModel.isFirstInSection) CORNER_RADIUS.dp else 0.dp,
        topEnd = if (viewModel.isFirstInSection) CORNER_RADIUS.dp else 0.dp,
        bottomStart = if (viewModel.isLastInSection) CORNER_RADIUS.dp else 0.dp,
        bottomEnd = if (viewModel.isLastInSection) CORNER_RADIUS.dp else 0.dp,
    )
    val isEnabled by viewModel.isEnabled.collectAsState(true)

    Column(
        modifier = Modifier.background(Color.White, shape)
            .clip(shape)
            .padding(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
                .alpha(if (isEnabled) 1.0f else 0.3f)
                .padding(
                    top = if (viewModel.isFirstInSection) 16.dp else 12.dp,
                    bottom = if (viewModel.isLastInSection) 16.dp else 12.dp,
                )
        ) {
            Column(
                modifier = Modifier.weight(0.8f, fill = false)
            ) {
                Text(
                    stringResource(viewModel.title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface

                )
                Text(
                    stringResource(viewModel.description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            val value = viewModel.getValue()
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is Boolean -> {
                    SettingsBooleanInput(viewModel as SettingsItemViewModel<Boolean>)
                }

                // UInt and ULong are not handled as Number types in Kotlin so we need to handle
                // those in a separate when branch
                is UInt, ULong -> {
                    SettingsNumericalInput(viewModel)
                }

                is Number -> {
                    SettingsNumericalInput(viewModel)
                }

                is IterableEnum<*> -> {
                    SettingsEnumInput(viewModel as SettingsEnumItemViewModel<*>)
                }
            }
        }

        if (!viewModel.isLastInSection) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}
