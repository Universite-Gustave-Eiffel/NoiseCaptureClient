package org.noiseplanet.noisecapture

import IgnoreIos
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.noiseplanet.noisecapture.model.dao.Questionnaire
import kotlin.test.Test
import kotlin.test.assertIs


class QuestionnaireTest {

    private val questionnairePaths = listOf(
        Path("src/commonMain/composeResources/files/test_questionnaire.json"),
        Path("src/commonMain/composeResources/files/soundscape_default_questionnaire.json"),
    )

    /**
     * Asserts that bundled questionnaires can be properly deserialized to a [Questionnaire] object.
     */
    @Test
    @IgnoreIos // Ignore this test on iOS as app resources are unaccessible in test environment due to sandboxing
    fun testDeserializingQuestionnaire() {
        questionnairePaths.forEach { path ->
            val contents = SystemFileSystem.source(path).buffered().use {
                it.readByteArray().decodeToString()
            }
            val questionnaire = Json.decodeFromString<Questionnaire>(contents)
            assertIs<Questionnaire>(questionnaire)
        }
    }
}
