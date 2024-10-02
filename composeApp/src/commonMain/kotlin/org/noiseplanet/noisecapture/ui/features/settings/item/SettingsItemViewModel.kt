package org.noiseplanet.noisecapture.ui.features.settings.item

import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.services.SettingsKey
import org.noiseplanet.noisecapture.services.UserSettingsService

class SettingsItemViewModel<T : Any>(
    val title: StringResource,
    val description: StringResource,
    val isFirstInSection: Boolean = false,
    val isLastInSection: Boolean = false,
    val settingKey: SettingsKey<T>,

    private val settingsService: UserSettingsService,
) {

    fun getValue(): T? = settingsService.get(settingKey)
    fun getValue(defaultValue: T): T = settingsService.get(settingKey, defaultValue)

    fun getValueFlow(): Flow<T?> = settingsService.getFlow(settingKey)
    fun getValueFlow(defaultValue: T) = settingsService.getFlow(settingKey, defaultValue)

    fun setValue(newValue: T?) = settingsService.set(settingKey, newValue)
}
