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
                modifier = Modifier.clickable {
                    isDropDownExpanded.value = true
                }
            ) {
                Text(
                    text = stringResource(selectedItemName),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }) {
                viewModel.choices.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(name),
                                style = MaterialTheme.typography.bodySmall
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
