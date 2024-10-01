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

    private val settingKey: SettingsKey<T>,
    private val settingsService: UserSettingsService,
) {

    fun getValue(): T? = settingsService.get(settingKey)
    fun getValueFlow(): Flow<T?> = settingsService.getFlow(settingKey)
}
