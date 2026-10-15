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
import noisecapture.composeapp.generated.resources.skip
import noisecapture.composeapp.generated.resources.submit
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Questionnaire
import org.noiseplanet.noisecapture.model.dao.QuestionnaireAnswers
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class QuestionnaireScreenViewModel(
    val measurementUuid: String,
    val questionnaireId: String = "soundscape_default_questionnaire",
) : ViewModel(), ScreenViewModel, KoinComponent {

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

    private val measurementService: MeasurementService by inject()

    override val title: StringResource
        get() = Res.string.questionnaire_title

    override val hideBackButton: Boolean
        get() = true

    override val confirmPopBackStack: () -> Boolean
        get() = { false }

    override val actions: StateFlow<List<AppBarButtonViewModel>> = MutableStateFlow(
        listOf(
            AppBarButtonViewModel(
                icon = null,
                title = Res.string.skip,
                onClick = { onQuestionnaireDone?.invoke(measurementUuid) },
            )
        )
    )

    private val _viewStateFlow = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewStateFlow

    var onQuestionnaireDone: ((measurementUuid: String) -> Unit)? = null


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

    fun confirmPage(answers: QuestionnaireAnswers) {
        val currentState = viewState.value as? ViewState.QuestionnaireState ?: return
        val currentIndex = currentState.currentPageIndex
        val pagesCount = currentState.questionnaire.pages.size

        if (currentIndex == pagesCount - 1) {
            // If this is the last page, save questionnaire answers and trigger completion callback
            viewModelScope.launch {
                measurementService.saveQuestionnaireAnswersForMeasurement(measurementUuid, answers)
                onQuestionnaireDone?.invoke(measurementUuid)
            }
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

    private fun viewStateForPage(pageIndex: Int): ViewState? {
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
