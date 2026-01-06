package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel

/**
 * A custom text field for numerical settings values.
 *
 * @param viewModel View model. Must be of type [Int], [Float], or [Double].
 * @param modifier Compose [Modifier]
 */
@Composable
fun <T : Any> SettingsNumericalInput(
    viewModel: SettingsItemViewModel<T>,
    modifier: Modifier = Modifier,
) {
    // - Properties

    var textFieldValueState by remember {
        mutableStateOf(viewModel.getValue().toString())
    }
    val isEnabled by viewModel.isEnabled.collectAsState(true)

    var showEditDialog: Boolean by remember { mutableStateOf(false) }
    val confirmButtonViewModel = NCButtonViewModel(
        title = Res.string.save,
        colors = { NCButtonColors.Defaults.secondary() }
    )
    val cancelButtonViewModel = NCButtonViewModel(
        title = Res.string.cancel,
        style = NCButtonStyle.TEXT,
        colors = { NCButtonColors.Defaults.text() }
    )


    // - Private functions

    @Suppress("UNCHECKED_CAST")
    fun getNumericalValue(): T? {
        // TODO: Add value range validation
        return when (viewModel.settingKey.defaultValue) {
            is Int -> textFieldValueState.toIntOrNull() as T?
            is UInt -> textFieldValueState.toUIntOrNull() as T?
            is Long -> textFieldValueState.toLongOrNull() as T?
            is ULong -> textFieldValueState.toULongOrNull() as T?
            is Double -> textFieldValueState.toDoubleOrNull() as T?
            is Float -> textFieldValueState.toFloatOrNull() as T?
            else -> null
        }
    }

    fun saveEdit() {
        getNumericalValue()?.let { newValue ->
            viewModel.setValue(newValue)
            showEditDialog = false
        }
    }

    fun cancelEdit() {
        textFieldValueState = viewModel.getValue().toString()
        showEditDialog = false
    }


    // - Layout

    Box(
        modifier = modifier.width(IntrinsicSize.Min)
            .clickable {
                showEditDialog = true
            }
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = viewModel.getValue().toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface
                    .copy(alpha = if (isEnabled) 1.0f else 0.5f)
            ),
            modifier = Modifier.widthIn(min = 32.dp, max = 64.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (showEditDialog) {
        AlertDialog(
            title = {
                Text(text = stringResource(viewModel.title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = stringResource(viewModel.description))

                    TextField(
                        value = textFieldValueState,
                        onValueChange = { newValue ->
                            textFieldValueState = newValue
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { saveEdit() }),
                        isError = getNumericalValue() == null,
                        maxLines = 1,
                    )
                }
            },
            onDismissRequest = { cancelEdit() },
            confirmButton = {
                NCButton(
                    viewModel = confirmButtonViewModel,
                    onClick = { saveEdit() }
                )
            },
            dismissButton = {
                NCButton(
                    viewModel = cancelButtonViewModel,
                    onClick = { cancelEdit() }
                )
            },
            modifier = Modifier.imePadding()
        )
    }
}
