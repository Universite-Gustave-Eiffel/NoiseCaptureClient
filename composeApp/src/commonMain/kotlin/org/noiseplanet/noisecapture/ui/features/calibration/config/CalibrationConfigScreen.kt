package org.noiseplanet.noisecapture.ui.features.calibration.config

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.jacobras.humanreadable.HumanReadable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_duration_select_title
import noisecapture.composeapp.generated.resources.calibration_frequencies_select_title
import noisecapture.composeapp.generated.resources.calibration_frequencies_whole_spectrum_description
import noisecapture.composeapp.generated.resources.calibration_from_reference_intro_description
import noisecapture.composeapp.generated.resources.calibration_from_reference_intro_title
import noisecapture.composeapp.generated.resources.calibration_microphone_current_gain
import noisecapture.composeapp.generated.resources.calibration_microphone_last_calibrated
import noisecapture.composeapp.generated.resources.calibration_microphone_not_calibrated
import noisecapture.composeapp.generated.resources.calibration_microphone_select_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.model.dao.MicrophoneCalibrationProfile
import org.noiseplanet.noisecapture.model.enums.CalibrationFrequencyBand
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.micselect.MicrophoneSelectView
import org.noiseplanet.noisecapture.ui.features.calibration.calibrationModule
import org.noiseplanet.noisecapture.ui.navigation.router.CalibrationRouter
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.AdaptiveUtil
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets
import org.noiseplanet.noisecapture.util.toSignedString
import kotlin.time.Instant


@OptIn(KoinExperimentalAPI::class)
@Composable
fun CalibrationConfigScreen(
    viewModel: CalibrationConfigScreenViewModel,
    router: CalibrationRouter,
) {
    // - DI

    rememberKoinModules {
        listOf(calibrationModule)
    }


    // - Properties

    val defaultCalibrationDurationSeconds = 10

    var showFrequencyBandsSelectMenu: Boolean by rememberSaveable {
        mutableStateOf(false)
    }
    var selectedDurationSeconds: Int by rememberSaveable {
        mutableStateOf(defaultCalibrationDurationSeconds)
    }
    var selectedFrequencyBand: CalibrationFrequencyBand by rememberSaveable {
        mutableStateOf(CalibrationFrequencyBand.WHOLE_SPECTRUM)
    }
    val currentCalibrationProfile: MicrophoneCalibrationProfile? by viewModel.currentCalibrationProfile
        .collectAsStateWithLifecycle()


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = AdaptiveUtil.MAX_FULL_SCREEN_WIDTH)
                    .padding(16.dp)
                    .paddingBottomWithInsets()
            ) {
                // Introduction section
                Column {
                    Text(
                        text = stringResource(Res.string.calibration_from_reference_intro_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(Res.string.calibration_from_reference_intro_description),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // TODO: Add tips section

                // Microphone select section
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.calibration_microphone_select_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    MicrophoneSelectView(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    )

                    currentCalibrationProfile?.let { calibrationProfile ->
                        val calibrationProfileText = buildAnnotatedString {
                            if (calibrationProfile.isCalibrated) {
                                append(stringResource(Res.string.calibration_microphone_last_calibrated) + " ")
                                val datetime =
                                    Instant.fromEpochMilliseconds(calibrationProfile.calibrationTimestamp)
                                append(HumanReadable.timeAgo(datetime) + "\n")
                                append(stringResource(Res.string.calibration_microphone_current_gain) + " ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("${calibrationProfile.compensationGain.toSignedString()} dB(A)")
                                }
                            } else {
                                append(stringResource(Res.string.calibration_microphone_not_calibrated))
                            }
                        }
                        Text(
                            text = calibrationProfileText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Duration and frequency band select section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(Res.string.calibration_duration_select_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        CalibrationDurationSelectView(
                            onSelectedDurationChange = { selectedDurationSeconds = it },
                            initialValue = selectedDurationSeconds,
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(Res.string.calibration_frequencies_select_title),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Box(
                            modifier = Modifier.clickable { showFrequencyBandsSelectMenu = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedFrequencyBand.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            FrequencyBandSelectMenu(
                                expanded = showFrequencyBandsSelectMenu,
                                onDismissRequest = { showFrequencyBandsSelectMenu = false },
                                onSelectFrequencyBand = {
                                    selectedFrequencyBand = it
                                    showFrequencyBandsSelectMenu = false
                                },
                            )
                        }
                    }
                }

                NCButton(
                    viewModel = viewModel.startButtonViewModel,
                    onClick = {
                        router.onClickStartCalibration(
                            durationSeconds = selectedDurationSeconds,
                            frequencyBand = selectedFrequencyBand
                        )
                    },
                    modifier = Modifier.height(50.dp)
                )
            }
        }
    }
}


@Composable
private fun FrequencyBandSelectMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectFrequencyBand: (CalibrationFrequencyBand) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = NoiseLevelColorRamp.level1Light,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        for (item in CalibrationFrequencyBand.entries.sortedBy { it.centerFrequency }) {
            DropdownMenuItem(
                text = {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        if (item == CalibrationFrequencyBand.WHOLE_SPECTRUM) {
                            Text(
                                text = stringResource(Res.string.calibration_frequencies_whole_spectrum_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                onClick = {
                    onSelectFrequencyBand(item)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
