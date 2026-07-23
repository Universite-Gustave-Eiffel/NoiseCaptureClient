package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.questionnaire_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.model.dao.Questionnaire
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class QuestionnaireScreenViewModel(
    val questionnaireId: String = "soundscape_default_questionnaire",
) : ViewModel(), ScreenViewModel {

    // - Associated types

    sealed interface ViewState {

        data object Loading : ViewState

        data class QuestionnaireState(
            val questionnaire: Questionnaire,
            val currentPageIndex: Int,
        ) : ViewState
    }


    // - Properties

    override val title: StringResource
        get() = Res.string.questionnaire_title

    private val _viewStateFlow = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewStateFlow


    // - Lifecycle

    init {
        viewModelScope.launch {
            val questionnaireData = Res.readBytes("files/$questionnaireId.json").decodeToString()
            val questionnaire = Json.decodeFromString<Questionnaire>(questionnaireData)

            _viewStateFlow.tryEmit(
                ViewState.QuestionnaireState(
                    questionnaire = questionnaire,
                    currentPageIndex = 0,
                )
            )
        }
    }


    // - Public functions

    fun nextPage() {
        val currentState = viewState.value as? ViewState.QuestionnaireState ?: return
        _viewStateFlow.tryEmit(
            currentState.copy(currentPageIndex = currentState.currentPageIndex + 1)
        )
    }
}
