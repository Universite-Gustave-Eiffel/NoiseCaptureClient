package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun SettingsBooleanItem(
    viewModel: SettingsItemViewModel<Boolean>,
    defaultValue: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val value by viewModel.getValueFlow(defaultValue)
        .collectAsState(viewModel.getValue(defaultValue))

    Switch(
        checked = value,
        onCheckedChange = { newValue ->
            viewModel.setValue(newValue)
        },
        modifier
    )
}

@Composable
fun SettingsIntegerItem(
    viewModel: SettingsItemViewModel<Int>,
    defaultValue: Int = 0,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val stringValue = remember {
        mutableStateOf(viewModel.getValue(defaultValue).toString())
    }

    TextField(
        value = stringValue.value,

        // Update state on value change
        onValueChange = { newValue ->
            // Filter out whitespaces and decimal separators
            stringValue.value = newValue
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "")
        },

        textStyle = MaterialTheme.typography.titleMedium.copy(),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        maxLines = 1,

        // Use a numerical keyboard with a Done button
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),

        // Add listener for keyboard Done button press
        keyboardActions = KeyboardActions(onDone = {
            // Save new value to settings if valid
            stringValue.value.toIntOrNull()?.let {
                viewModel.setValue(it)
                // Clear focus and dismiss keyboard
                focusManager.clearFocus()
            }
        }),

        // Input validation
        isError = stringValue.value.toIntOrNull() == null,

        modifier = modifier
    )
}

// TODO: Reduce code duplication between different text fields
@Composable
fun SettingsDoubleItem(
    viewModel: SettingsItemViewModel<Double>,
    defaultValue: Double = 0.0,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val stringValue = remember {
        mutableStateOf(viewModel.getValue(defaultValue).toString())
    }

    TextField(
        value = stringValue.value,

        // Update state on value change
        onValueChange = { newValue ->
            // Filter out whitespaces
            stringValue.value = newValue
                .replace(" ", "")
        },

        textStyle = MaterialTheme.typography.titleMedium,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        maxLines = 1,

        // Use a numerical keyboard with a Done button
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),

        // Add listener for keyboard Done button press
        keyboardActions = KeyboardActions(onDone = {
            // Save new value to settings if valid
            stringValue.value.toDoubleOrNull()?.let {
                viewModel.setValue(it)
                // Clear focus and dismiss keyboard
                focusManager.clearFocus()
            }
        }),

        // Input validation
        isError = stringValue.value.toDoubleOrNull() == null,

        modifier = modifier.width(IntrinsicSize.Min)
    )
}

@Composable
fun SettingsFloatItem(
    viewModel: SettingsItemViewModel<Float>,
    defaultValue: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val stringValue = remember {
        mutableStateOf(viewModel.getValue(defaultValue).toString())
    }

    TextField(
        value = stringValue.value,

        // Update state on value change
        onValueChange = { newValue ->
            // Filter out whitespaces
            stringValue.value = newValue
                .replace(" ", "")
        },

        textStyle = MaterialTheme.typography.titleMedium,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        maxLines = 1,

        // Use a numerical keyboard with a Done button
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),

        // Add listener for keyboard Done button press
        keyboardActions = KeyboardActions(onDone = {
            // Save new value to settings if valid
            stringValue.value.toFloatOrNull()?.let {
                viewModel.setValue(it)
                // Clear focus and dismiss keyboard
                focusManager.clearFocus()
            }
        }),

        // Input validation
        isError = stringValue.value.toFloatOrNull() == null,

        modifier = modifier.width(IntrinsicSize.Min)
    )
}
