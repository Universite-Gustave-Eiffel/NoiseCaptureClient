package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsEnumInput(
    viewModel: SettingsEnumItemViewModel<*>,
) {
    val isDropDownExpanded = remember {
        mutableStateOf(false)
    }
    val isEnabled by viewModel.isEnabled
        .collectAsState(true)

    val selectedItemName by viewModel.selected
        .collectAsState(initial = viewModel.initialValue)

    Column(
        modifier = Modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = isEnabled) {
                    isDropDownExpanded.value = true
                }
            ) {
                Text(
                    text = stringResource(selectedItemName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = if (isEnabled) 1.0f else 0.5f)
                )
            }
            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                },
            ) {
                viewModel.choices.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(name),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            viewModel.select(index)
                            isDropDownExpanded.value = false
                        }
                    )
                }
            }
        }

    }
}
