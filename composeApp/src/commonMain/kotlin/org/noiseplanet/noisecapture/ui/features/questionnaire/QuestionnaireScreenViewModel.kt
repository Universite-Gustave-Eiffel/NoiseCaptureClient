package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.continue_
import noisecapture.composeapp.generated.resources.questionnaire_confirm_skip_body
import noisecapture.composeapp.generated.resources.questionnaire_confirm_skip_title
import noisecapture.composeapp.generated.resources.questionnaire_confirm_submit_body
import noisecapture.composeapp.generated.resources.questionnaire_confirm_submit_title
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
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialogViewModel
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


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

    interface Listener {

        fun onSkipQuestionnaire()
        fun onSubmitQuestionnaire()
        fun shouldNavigateToDetails()
    }


    // - Properties

    private val measurementService: MeasurementService by inject()

    private val _viewStateFlow = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewStateFlow

    val submitConfirmationDialogViewModel = NCDialogViewModel(
        title = Res.string.questionnaire_confirm_submit_title,
        text = Res.string.questionnaire_confirm_submit_body,
        confirmButtonContent = ButtonContent(title = Res.string.submit),
        confirmButtonColors = Color.Noise.one.tertiaryContainerColors(hasDropShadow = true),
        dismissButtonContent = ButtonContent(title = Res.string.cancel),
    )
    val skipConfirmationDialogViewModel = NCDialogViewModel(
        title = Res.string.questionnaire_confirm_skip_title,
        text = Res.string.questionnaire_confirm_skip_body,
        confirmButtonContent = ButtonContent(title = Res.string.skip),
        confirmButtonColors = Color.Noise.six.tertiaryContainerColors(hasDropShadow = true),
        dismissButtonContent = ButtonContent(title = Res.string.cancel),
    )
    var listener: Listener? = null

    override val title: StringResource
        get() = Res.string.questionnaire_title

    override val hideBackButton: Boolean
        get() = true

    override val confirmPopBackStack: () -> Boolean
        get() = { false }

    override val actions: StateFlow<List<AppBarButtonViewModel>> = _viewStateFlow.map { state ->
        (state as? ViewState.QuestionnaireState)?.let {
            if (it.questionnaire.skippable) {
                listOf(
                    AppBarButtonViewModel(
                        icon = null,
                        title = Res.string.skip,
                        onClick = { listener?.onSkipQuestionnaire() },
                    )
                )
            } else {
                listOf()
            }
        } ?: listOf()
    }.stateInWhileSubscribed(viewModelScope, listOf())


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
            // If this is the last page, trigger completion callback
            listener?.onSubmitQuestionnaire()
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

    fun submitAnswers(answers: QuestionnaireAnswers) {
        viewModelScope.launch {
            measurementService.saveQuestionnaireAnswersForMeasurement(measurementUuid, answers)
            listener?.shouldNavigateToDetails()
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
