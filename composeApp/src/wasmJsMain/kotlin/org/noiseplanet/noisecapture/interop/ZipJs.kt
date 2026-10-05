@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("UnusedParameter", "UnusedPrivateProperty")

package org.noiseplanet.noisecapture.interop

import org.w3c.files.Blob
import kotlin.js.Promise

@JsModule("@zip.js/zip.js")
external object ZipJs {

    class BlobWriter(contentType: String) {

        fun getData(): Promise<Blob>
    }

    class ZipWriter(
        writer: BlobWriter,
        options: ZipWriterOptions = definedExternally,
    ) {

        fun add(
            name: String,
            reader: Reader,
            options: ZipAddOptions = definedExternally,
        ): Promise<EntryMetaData>

        fun close(): Promise<JsAny?>
    }

    interface Reader : JsAny

    interface EntryMetaData : JsAny

    class BlobReader(data: Blob) : Reader
}

@JsModule("@zip.js/zip.js")
external interface ZipWriterOptions {

    var level: Int?
}

@JsModule("@zip.js/zip.js")
external interface ZipAddOptions {

    var level: Int?
    var onprogress: ((index: Int, total: Int) -> Unit)?
}
