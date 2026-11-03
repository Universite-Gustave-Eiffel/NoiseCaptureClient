package org.noiseplanet.noisecapture.services.questionnaire

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import noisecapture.composeapp.generated.resources.Res
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.Questionnaire
import org.noiseplanet.noisecapture.services.questionnaire.QuestionnaireService.Companion.DEFAULT_QUESTIONNAIRE_ID
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.services.storage.injectStorageService
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * Default questionnaire service implementation
 */
class DefaultQuestionnaireService : QuestionnaireService, KoinComponent {

    // - Constants

    private companion object {

        const val LOCAL_QUESTIONNAIRES_DIR: String = "files"
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val storageService: StorageService<Questionnaire> by injectStorageService()


    // - QuestionnaireService

    override suspend fun getQuestionnaire(id: String): Questionnaire? {
        if (id == DEFAULT_QUESTIONNAIRE_ID) {
            // Default questionnaire is bundled in the app's files
            val path = "$LOCAL_QUESTIONNAIRES_DIR/$DEFAULT_QUESTIONNAIRE_ID.json"
            return deserializeQuestionnaire(jsonData = Res.readBytes(path).decodeToString())
        }
        // In case of a custom questionnaire, try to get it from cache, or download it
        return getLocalQuestionnaire(id) ?: getRemoteQuestionnaire(id)
    }


    // - Private functions

    private suspend fun getLocalQuestionnaire(id: String): Questionnaire? {
        return storageService.get(id)
    }

    private suspend fun getRemoteQuestionnaire(id: String): Questionnaire? {
        // TODO
        return null
    }

    private fun deserializeQuestionnaire(jsonData: String): Questionnaire? {
        return try {
            Json.decodeFromString<Questionnaire>(jsonData)
        } catch (error: SerializationException) {
            logger.error("Error while deserialising questionnaire", error)
            null
        } catch (error: IllegalArgumentException) {
            logger.error("Error while deserialising questionnaire", error)
            null
        }
    }
}
