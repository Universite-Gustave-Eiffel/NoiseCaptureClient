package org.noiseplanet.noisecapture.ui.features.settings.item

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.services.SettingsKey
import org.noiseplanet.noisecapture.services.UserSettingsService
import org.noiseplanet.noisecapture.util.IterableEnum
import org.noiseplanet.noisecapture.util.ShortNameRepresentable

/**
 * Base setting item view model class to use with primitive types.
 */
@Suppress("LongParameterList")
open class SettingsItemViewModel<T>(
    val title: StringResource,
    val description: StringResource,
    val isFirstInSection: Boolean = false,
    val isLastInSection: Boolean = false,
    val isEnabled: Flow<Boolean> = flow { emit(true) },
    val settingKey: SettingsKey<T>,

    protected val settingsService: UserSettingsService,
) {

    /**
     * Returns this setting's value directly
     */
    fun getValue(): T = settingsService.get(settingKey)

    /**
     * Gets this setting's value as a flow to listen for value changes.
     */
    fun getValueFlow(): Flow<T> = settingsService.getFlow(settingKey)

    /**
     * Sets a new value for this setting key.
     */
    fun setValue(newValue: T?) = settingsService.set(settingKey, newValue)
}


/**
 * A SettingItem subclass to be used with enums that can be stored in user defaults.
 *
 * The given enum must comply to both [IterableEnum] and [ShortNameRepresentable].
 */
@Suppress("LongParameterList")
class SettingsEnumItemViewModel<T>(
    title: StringResource,
    description: StringResource,
    isFirstInSection: Boolean = false,
    isLastInSection: Boolean = false,
    isEnabled: Flow<Boolean> = flow { emit(true) },
    settingKey: SettingsKey<T>,
    settingsService: UserSettingsService,
) : SettingsItemViewModel<T>(
    title = title,
    description = description,
    isFirstInSection = isFirstInSection,
    isLastInSection = isLastInSection,
    isEnabled = isEnabled,
    settingKey = settingKey,
    settingsService = settingsService
) where T : Enum<T>, T : IterableEnum<T>, T : ShortNameRepresentable {

    private val entries = settingKey.defaultValue.entries()

    /**
     * Lists the choices that will be available in the dropdown menu
     */
    val choices: List<StringResource> = entries
        .map { it.fullName }

    /**
     * Returns the currently selected item as a string resource
     */
    val selected: Flow<StringResource> = getValueFlow()
        .map { it.shortName }

    /**
     * The initial value to be displayed as a string resource
     */
    val initialValue: StringResource = getValue().shortName

    /**
     * Select a new value and update the underlying setting value from the index
     * of the selected choice.
     */
    fun select(index: Int) {
        setValue(entries[index])
    }
}
