package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * A custom text field for numerical settings values.
 *
 * @param viewModel View model. Must be of type [Int], [Float], or [Double].
 * @param modifier Compose [Modifier]
 */
@Composable
fun <T : Any> SettingsNumericalItem(
    viewModel: SettingsItemViewModel<T>,
    modifier: Modifier = Modifier,
) {
    @Suppress("UNCHECKED_CAST")
    val defaultValue = when (viewModel.settingKey.defaultValue) {
        is Int -> 0 as T
        is Double -> 0.0 as T
        is Float -> 0.0f as T
        else -> error("Template parameter must be a numerical value")
    }

    var textFieldValueState by remember {
        val initialValue = viewModel.getValue().toString()

        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val colors = TextFieldDefaults.colors(
        unfocusedContainerColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )

    @Suppress("UNCHECKED_CAST")
    fun getNumericalValue(): T? {
        return when (viewModel.settingKey.defaultValue) {
            is Int -> textFieldValueState.text.toIntOrNull() as T?
            is Double -> textFieldValueState.text.toDoubleOrNull() as T?
            is Float -> textFieldValueState.text.toFloatOrNull() as T?
            else -> null
        }
    }


    Box(
        modifier = modifier.width(IntrinsicSize.Min)
    ) {
        BasicTextField(
            value = textFieldValueState,
            onValueChange = { newValue ->
                textFieldValueState = newValue
                // If entered value is valid, save it
                getNumericalValue()?.let {
                    viewModel.setValue(it)
                }
            },
            textStyle = MaterialTheme.typography.titleSmall.copy(textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                // Clear focus and dismiss keyboard
                focusManager.clearFocus()
            }),
            singleLine = true,
            modifier = modifier
                .widthIn(min = 32.dp, max = 64.dp)
                .align(Alignment.CenterEnd)
                .padding(vertical = 16.dp)
                .padding(start = 4.dp)
                .disabledHorizontalPointerInputScroll()
        ) {
            TextFieldDefaults.DecorationBox(
                value = textFieldValueState.text,
                innerTextField = it,
                singleLine = true,
                enabled = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                isError = getNumericalValue() == null,
                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                    start = 0.dp,
                    end = 0.dp,
                    top = 0.dp,
                    bottom = 0.dp,
                ),
                prefix = null,
                colors = colors,
            )
        }
    }
}


/**
 * Fixes a weird scrolling behaviour when using BasicTextField with width based on IntrinsicSize.min:
 * https://stackoverflow.com/questions/73309395/strange-scroll-in-the-basictextfield-with-intrinsicsize-min
 */
private val HorizontalScrollConsumer = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource) = available.copy(y = 0f)
    override suspend fun onPreFling(available: Velocity) = available.copy(y = 0f)
}

fun Modifier.disabledHorizontalPointerInputScroll(disabled: Boolean = true) =
    if (disabled) {
        this.nestedScroll(HorizontalScrollConsumer)
    } else {
        this
    }