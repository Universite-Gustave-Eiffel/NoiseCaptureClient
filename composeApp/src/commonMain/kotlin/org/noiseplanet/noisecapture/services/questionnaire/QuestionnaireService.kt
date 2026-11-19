package org.noiseplanet.noisecapture.services.questionnaire

import org.noiseplanet.noisecapture.model.dao.Questionnaire

/**
 * Fetch local or distant questionnaire definitions
 */
interface QuestionnaireService {

    // - Constants

    companion object {

        const val DEFAULT_QUESTIONNAIRE_ID: String = "soundscape_default_questionnaire"
    }


    // - Public functions

    /**
     * Fetches a questionnaire with the given identifier. If available locally, get it from file
     * system. Otherwise, try to download it from remote server.
     */
    suspend fun getQuestionnaire(id: String): Questionnaire?
}
