package org.noiseplanet.noisecapture.ui.features.calibration

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_results_already_calibrated
import noisecapture.composeapp.generated.resources.calibration_results_current_gain
import noisecapture.composeapp.generated.resources.calibration_results_difference
import noisecapture.composeapp.generated.resources.calibration_results_reference_value
import noisecapture.composeapp.generated.resources.calibration_results_reference_value_placeholder
import noisecapture.composeapp.generated.resources.calibration_results_save_gain
import noisecapture.composeapp.generated.resources.calibration_results_suggested_gain
import noisecapture.composeapp.generated.resources.calibration_results_suggested_gain_warning
import noisecapture.composeapp.generated.resources.calibration_results_your_device_value
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.ui.navigation.router.CalibrationRouter
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.AdaptiveUtil
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets
import org.noiseplanet.noisecapture.util.roundTo
import org.noiseplanet.noisecapture.util.toSignedString
import kotlin.math.absoluteValue


@Composable
fun CalibrationResultsView(
    viewModel: CalibrationScreenViewModel,
    viewState: CalibrationScreenViewModel.ViewState.Results,
    router: CalibrationRouter,
) {
    // - Properties

    var referenceDeviceFieldValue: String by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }


    // - Lifecycle

    DisposableEffect(Unit) {
        // Show keyboard right away
        focusRequester.requestFocus()
        onDispose { }
    }


    // - Layout

    Column(
        modifier = Modifier.fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .widthIn(max = AdaptiveUtil.MAX_FULL_SCREEN_WIDTH)
            .padding(top = 24.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            }
    ) {
        Text(
            text = stringResource(Res.string.calibration_results_current_gain),
            style = MaterialTheme.typography.titleMedium,
            color = NoiseLevelColorRamp.level1Dark,
            modifier = Modifier.padding(horizontal = 16.dp + 24.dp)
        )
        Text(
            text = "${viewState.currentGain.toSignedString()} dB(A)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = NoiseLevelColorRamp.level1Dark,
            modifier = Modifier.padding(horizontal = 16.dp + 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.large
                ).padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 10.dp)
        ) {
            // Current device measured value
            Text(
                text = stringResource(Res.string.calibration_results_your_device_value),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 45.sp)) {
                        append(viewState.measuredValue.roundTo(1).toString() + " ")
                    }
                    append("dB(A)")
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(y = (-8).dp)
            )

            // Divider
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.height(1.dp)
                    .fillMaxWidth()
                    .background(color = NoiseLevelColorRamp.level1.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Reference device measured value
            Text(
                text = stringResource(Res.string.calibration_results_reference_value),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            ReferenceDeviceValueField(
                value = referenceDeviceFieldValue,
                onValueChange = {
                    // Replace commas with dots for locales that use commas as decimal separator
                    referenceDeviceFieldValue = it.replace(",", ".")
                    viewModel.onReferenceValueChange(referenceDeviceFieldValue.toDoubleOrNull())
                },
                interactionSource = interactionSource,
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    focusRequester.freeFocus()
                },
                isError = referenceDeviceFieldValue.isNotEmpty() &&
                    referenceDeviceFieldValue.toDoubleOrNull() == null,
                modifier = Modifier.focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            keyboardController?.show()
                        }
                    }
            )
        }

        viewState.difference?.let { difference ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.calibration_results_difference),
                style = MaterialTheme.typography.titleMedium,
                color = NoiseLevelColorRamp.level1Dark,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(horizontal = 16.dp + 24.dp).fillMaxWidth(),
            )

            val differenceText = if (difference.absoluteValue < 0.5) {
                "<0.5 dB(A)"
            } else {
                "${difference.toSignedString()} dB(A)"
            }
            Text(
                text = differenceText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = NoiseLevelColorRamp.level1Dark,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(horizontal = 16.dp + 24.dp).fillMaxWidth(),
            )
            if (difference.absoluteValue < 0.5) {
                Text(
                    text = stringResource(Res.string.calibration_results_already_calibrated),
                    style = MaterialTheme.typography.bodySmall,
                    color = NoiseLevelColorRamp.level1Dark,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(horizontal = 16.dp + 24.dp).fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.weight(1f))

        // Suggested compensation gain
        viewState.suggestedGain?.let { suggestedGain ->
            val (containerColor, contentColor) = if (viewState.isWarning) {
                Pair(NoiseLevelColorRamp.level6Dark, NoiseLevelColorRamp.level6Light)
            } else {
                Pair(NoiseLevelColorRamp.level1Dark, NoiseLevelColorRamp.level1Light)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        color = containerColor,
                        shape = MaterialTheme.shapes.large.copy(
                            bottomStart = ZeroCornerSize,
                            bottomEnd = ZeroCornerSize
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
                    .paddingBottomWithInsets(withNavBar = 12.dp, withoutNavBar = 24.dp),
            ) {
                Text(
                    text = stringResource(Res.string.calibration_results_suggested_gain),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = MaterialTheme.typography.displayMedium.fontSize)) {
                            append(suggestedGain.toSignedString())
                        }
                        append(" dB(A)")
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                )

                if (viewState.isWarning) {
                    Text(
                        text = stringResource(Res.string.calibration_results_suggested_gain_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = contentColor,
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
                ) {
                    NCButton(
                        viewModel = NCButtonViewModel(
                            title = Res.string.cancel,
                            style = NCButtonStyle.TEXT,
                            colors = {
                                NCButtonColors.Defaults.text()
                                    .copy(contentColor = contentColor)
                            }
                        ),
                        onClick = { viewModel.cancelCalibration() },
                        modifier = Modifier.height(50.dp).weight(1f),
                    )
                    NCButton(
                        viewModel = NCButtonViewModel(
                            title = Res.string.calibration_results_save_gain,
                            colors = {
                                NCButtonColors(
                                    contentColor = containerColor,
                                    containerColor = contentColor,
                                )
                            },
                            hasDropShadow = true,
                        ),
                        onClick = {
                            viewModel.saveGain(viewState.suggestedGain) {
                                router.popBackStack()
                            }
                        },
                        modifier = Modifier.height(50.dp).weight(1f),
                    )
                }
            }
        }
    }
}


/**
 * Custom textfield styling for entering reference device measured value.
 */
@Composable
private fun ReferenceDeviceValueField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    interactionSource: InteractionSource,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.End,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        // Shorten cursor height a little so that it doesn't overlaps with top label
        cursorBrush = Brush.verticalGradient(
            0.00f to Color.Transparent,
            0.10f to Color.Transparent,
            0.10f to MaterialTheme.colorScheme.onSurface,
            0.90f to MaterialTheme.colorScheme.onSurface,
            0.90f to Color.Transparent,
            1.00f to Color.Transparent,
        ),
        keyboardActions = keyboardActions,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
        ),
        singleLine = true,
        modifier = modifier,
    ) { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = {
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    // Fix alignment of inner text field so it gets closer to the top label
                    modifier = Modifier.offset(y = (-6).dp).fillMaxWidth()
                ) {
                    innerTextField()
                }
            },
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = stringResource(Res.string.calibration_results_reference_value_placeholder),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.displayMedium.lineHeight,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            prefix = {
                Icon(
                    painter = painterResource(Res.drawable.edit),
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 12.dp).size(24.dp)
                )
            },
            suffix = {
                Text(
                    text = "dB(A)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedIndicatorColor = Color.Transparent,
                errorTextColor = MaterialTheme.colorScheme.error,
                errorContainerColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(0.dp)
        )
    }
}
