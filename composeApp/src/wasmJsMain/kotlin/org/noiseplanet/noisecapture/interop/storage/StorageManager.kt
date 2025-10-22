package org.noiseplanet.noisecapture.interop.storage

import kotlin.js.Promise

/**
 * Navigator StorageManager interop
 *
 * [MSDN documentation](https://developer.mozilla.org/en-US/docs/Web/API/StorageManager)
 */
@OptIn(ExperimentalWasmJsInterop::class)
external interface StorageManager {

    /**
     * Returns a Promise that resolves to an object containing usage and quota numbers for your origin.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/StorageManager/estimate)
     *
     * @return A [Promise] that resolves to an object with the following properties:
     *
     * `quota`: A numeric value in bytes which provides a conservative approximation of the total
     *  storage the user's device or computer has available for the site origin or Web app.
     *  It's possible that there's more than this amount of space available though you can't rely
     *  on that being the case.
     *
     * `usage`: A numeric value in bytes approximating the amount of storage space currently being used
     *  by the site or Web app, out of the available space as indicated by quota. Unit is byte.
     *
     * `usageDetails`: An object containing a breakdown of usage by storage system.
     *  All included properties will have a usage greater than 0 and any storage system with 0 usage
     *  will be excluded from the object.
     *
     * > Note: The returned values are not exact: between compression, deduplication, and obfuscation for security reasons, they will be imprecise.
     *
     * You may find that the quota varies from origin to origin. This variance is based on factors such as:
     *
     * - How often the user visits
     * - Public site popularity data
     * - User engagement signals like bookmarking, adding to homescreen, or accepting push notifications
     */
    fun estimate(): Promise<JsAny>

    /**
     * Used to obtain a reference to a [FileSystemDirectoryHandle] object allowing access to a directory
     * and its contents, stored in the origin private file system (OPFS).
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/StorageManager/getDirectory)
     *
     * @return A [Promise] that fulfills with a [FileSystemDirectoryHandle] object.
     */
    fun getDirectory(): Promise<FileSystemDirectoryHandle>

    /**
     * Requests permission to use persistent storage, and returns a [Promise] that resolves to `true`
     * if permission is granted and bucket mode is persistent, and false otherwise.
     * The browser may or may not honor the request, depending on browser-specific rules.
     * (For more details, see the guide to Storage quotas and eviction criteria.)
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/StorageManager/persist)
     *
     * @return A [Promise] that resolves to a [JsBoolean].
     */
    fun persist(): Promise<JsBoolean>

    /**
     * Returns a [Promise] that resolves to `true` if your site's storage bucket is persistent.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/StorageManager/persisted)
     *
     * @return A [Promise] that resolves to a [JsBoolean].
     */
    fun persisted(): Promise<JsBoolean>
}
