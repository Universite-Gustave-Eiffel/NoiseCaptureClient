package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.continue_
import noisecapture.composeapp.generated.resources.questionnaire_title
import noisecapture.composeapp.generated.resources.submit
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.model.dao.Questionnaire
import org.noiseplanet.noisecapture.ui.components.ButtonContent
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
            val confirmButtonContent: ButtonContent,
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
                    confirmButtonContent = ButtonContent(title = Res.string.continue_),
                )
            )
        }
    }


    // - Public functions

    fun confirmPage() {
        val currentState = viewState.value as? ViewState.QuestionnaireState ?: return
        val currentIndex = currentState.currentPageIndex
        val pagesCount = currentState.questionnaire.pages.size

        if (currentIndex == pagesCount - 1) {
            // TODO: Submit answers and leave questionnaire
        } else {
            viewStateForPage(currentIndex + 1)?.let {
                _viewStateFlow.tryEmit(it)
            }
        }
    }

    fun previousPage() {
        val currentState = viewState.value as? ViewState.QuestionnaireState ?: return
        val currentIndex = currentState.currentPageIndex

        viewStateForPage(currentIndex - 1)?.let {
            _viewStateFlow.tryEmit(it)
        }
    }


    // - Private functions

    fun viewStateForPage(pageIndex: Int): ViewState? {
        val currentState = viewState.value as? ViewState.QuestionnaireState ?: return null
        val pagesCount = currentState.questionnaire.pages.size

        return currentState.copy(
            currentPageIndex = pageIndex,
            confirmButtonContent = ButtonContent(
                title = if (pageIndex == pagesCount - 1) {
                    Res.string.submit
                } else {
                    Res.string.continue_
                }
            )
        )
    }
}
