package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.coroutines.await
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
    private val logger: Logger,
) : Codec<T> {

    // - Codec

    override suspend fun decode(): T? {
        // Get file and directory handles
        val (fileHandle, _) = OPFSHelper.getFileHandle(filePath) ?: return null
        val file = fileHandle.getFile().await<File>()

        // Create file reader
        val reader = FileReader()
        // Read contents from file. Since FileReader relies on a callback to get the contents
        // after reading, we wrap this in a suspendCoroutine to synchronise the result
        val fileContents = suspendCoroutine { continuation ->
            reader.readAsText(file)
            reader.addEventListener("load") {
                // Continue execution when contents are available.
                continuation.resume(reader.result)
            }
        }
        // Decode JSON from string
        return json.decodeFromString(serializer, fileContents.toString())
    }

    override suspend fun encode(value: T?) {
        // Get file and directory handles, create them if not found
        val (fileHandle, directoryHandle) = OPFSHelper.getFileHandle(
            filePath,
            createIfNotFound = true
        ) ?: return

        value?.let { unwrappedValue ->
            // Serialise data to JSON
            val data = json.encodeToString(serializer, unwrappedValue)
            // Get writer handle
            val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
            // Write data to file
            stream.write(data.toJsString()).await<Unit>()
            // Close writer handle
            stream.close().await<Unit>()
        } ?: run {
            // If value is null, delete the file
            directoryHandle.removeEntry(fileHandle.name).await<Unit>()
        }
    }
}


/**
 * Utility constructor for [KStoreOpfsCodec] using reified generic
 */
@Suppress("FunctionNaming")
inline fun <reified T : @Serializable Any> KStoreOpfsCodec(
    filePath: String,
    logger: Logger,
    json: Json = DefaultJson,
) = KStoreOpfsCodec<T>(
    filePath = filePath,
    json = json,
    serializer = json.serializersModule.serializer(),
    logger = logger,
)
