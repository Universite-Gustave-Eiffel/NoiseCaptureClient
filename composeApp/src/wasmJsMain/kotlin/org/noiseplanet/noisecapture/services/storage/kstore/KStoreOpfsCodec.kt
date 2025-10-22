package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.coroutines.await
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.noiseplanet.noisecapture.interop.storage.FileSystemWritableFileStream
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Custom KStore [Codec] implementation that serialises values to Json and stores
 * results in local storage using OPFS.
 *
 * @param filePath Path to the file that will be stored (e.g. "data/values.json").
 * @param json Json serialisation configuration.
 * @param serializer Serializer to use for values.
 * @param logger Logger for logging.
 */
@OptIn(ExperimentalWasmJsInterop::class)
class KStoreOpfsCodec<T : @Serializable Any>(
    private val filePath: String,
    private val json: Json,
    private val serializer: KSerializer<T>,
    private val version: Int,
    private val migration: Migration<T>,
    private val logger: Logger,
) : Codec<T> {

    // - Properties

    val versionFilePath: String = "$filePath.version"


    // - Codec

    override suspend fun decode(): T? {
        val jsonString = readFileContent(filePath) ?: return null

        // Try to decode JSON from string
        return try {
            json.decodeFromString(serializer, jsonString)
        } catch (e: SerializationException) {
            logger.warning(
                "Failed deserialization of object in file $filePath. " +
                    "This is probably due to a change in the model and should trigger a migration.",
                throwable = e,
            )

            val previousVersion: Int? = readFileContent(versionFilePath)?.let {
                json.decodeFromString(Int.serializer(), it)
            }
            val rawJsonData: JsonElement = json.decodeFromString(jsonString)
            migration(previousVersion, rawJsonData)
        }
    }

    override suspend fun encode(value: T?) {
        value?.let { unwrappedValue ->
            // Serialise data to JSON
            val data = json.encodeToString(serializer, unwrappedValue)
            // Write JSON data to file
            writeToFile(filePath, data)
            // Serialize version data
            val versionData = json.encodeToString(Int.serializer(), version)
            // Write version data to file
            writeToFile(versionFilePath, versionData)
        } ?: run {
            // Get file and directory handles if they exist
            val (fileHandle, directoryHandle) = OPFSHelper.getFileHandle(filePath) ?: return
            // If value is null, delete the file
            directoryHandle.removeEntry(fileHandle.name).await()
        }
    }


    // - Private functions

    private suspend fun readFileContent(filePath: String): String? {
        // Get file and directory handles
        val (fileHandle, _) = OPFSHelper.getFileHandle(filePath) ?: return null
        val file = fileHandle.getFile().await<File>()

        // Create file reader
        val reader = FileReader()
        // Read contents from file. Since FileReader relies on a callback to get the contents
        // after reading, we wrap this in a suspendCoroutine to synchronise the result
        return suspendCoroutine { continuation ->
            reader.readAsText(file)
            reader.addEventListener("load") {
                // Continue execution when contents are available.
                continuation.resume(reader.result?.toString())
            }
        }.toString()
    }

    private suspend fun writeToFile(filePath: String, data: String) {
        // Get file handle, create it if not found
        val (fileHandle, _) = OPFSHelper.getFileHandle(
            filePath,
            createIfNotFound = true
        ) ?: return
        // Get writer handle
        val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
        // Write data to file
        stream.write(data.toJsString()).await<Unit>()
        // Close writer handle
        stream.close().await<Unit>()
    }
}


/**
 * Utility constructor for [KStoreOpfsCodec] using reified generic
 */
@Suppress("FunctionNaming")
inline fun <reified T : @Serializable Any> KStoreOpfsCodec(
    filePath: String,
    version: Int,
    logger: Logger,
    noinline migration: Migration<T> = DefaultMigration(),
    json: Json = DefaultJson,
) = KStoreOpfsCodec<T>(
    filePath = filePath,
    json = json,
    version = version,
    migration = migration,
    serializer = json.serializersModule.serializer(),
    logger = logger,
)
