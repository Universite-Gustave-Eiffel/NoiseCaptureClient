package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_join_disclaimer
import noisecapture.composeapp.generated.resources.campaign_join_invite_code_body
import noisecapture.composeapp.generated.resources.campaign_join_invite_code_confirm
import noisecapture.composeapp.generated.resources.campaign_join_invite_code_title
import noisecapture.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialog
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun CampaignInviteCodeDialog(
    onDismissRequest: () -> Unit,
    onCampaignJoinRequest: (inviteCode: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val focusRequester = remember { FocusRequester() }
    var initialFocusRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var inviteCode: String by remember { mutableStateOf("") }

    val textStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 10.sp,
    )


    // - Layout

    NCDialog(
        title = {
            Text(text = stringResource(Res.string.campaign_join_invite_code_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(Res.string.campaign_join_invite_code_body))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    TextField(
                        value = inviteCode,
                        onValueChange = { newValue ->
                            if (newValue.length <= 6) {
                                inviteCode = newValue
                            }
                        },
                        placeholder = {
                            Text(text = "000000", style = textStyle, color = Color.Noise.two.dark)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { onDismissRequest }),
                        isError = !inviteCode.all { it.isDigit() },
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Noise.eight.light,
                            errorTextColor = Color.Noise.eight.dark,
                            errorCursorColor = Color.Noise.eight.dark,
                        ),
                        textStyle = textStyle,
                        modifier = Modifier.focusRequester(focusRequester)
                            .onGloballyPositioned {
                                if (!initialFocusRequested) {
                                    // Show keyboard automatically when opening the popup.
                                    focusRequester.requestFocus()
                                    initialFocusRequested = true
                                }
                            }
                    )
                }

                Text(
                    text = stringResource(Res.string.campaign_join_disclaimer),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Noise.two.dark,
                )
            }
        },
        confirmButton = {
            NCButton(
                content = ButtonContent(title = Res.string.campaign_join_invite_code_confirm),
                colors = Color.Noise.two.tertiaryContainerColors(hasDropShadow = true),
                onClick = {
                    isLoading = true
                    onCampaignJoinRequest(inviteCode)
                },
                enabled = isValidInviteCode(inviteCode) && !isLoading
            )
        },
        dismissButton = {
            NCButton(
                content = ButtonContent(title = Res.string.cancel),
                colors = Color.Noise.two.transparentContainerColors(),
                onClick = { onDismissRequest() },
            )
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}


private fun isValidInviteCode(code: String): Boolean {
    return code.length == 6 && code.all { it.isDigit() }
}
