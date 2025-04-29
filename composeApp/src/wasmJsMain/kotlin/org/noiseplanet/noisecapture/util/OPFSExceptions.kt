package org.noiseplanet.noisecapture.util


/**
 * Thrown when trying to access a file or directory that doesn't exist.
 *
 * @param path Path to the file or directory
 * @param cause Optional parent exception to keep stack strace
 */
internal class FileNotFoundException(
    path: String,
    cause: Throwable? = null,
) : Exception("$path not found.", cause)


/**
 * Thrown when OPFS features are not available in current context (unsecure connexion for instance).
 *
 * @param cause Optional parent exception to keep stack strace
 */
internal class OPFSUnavailableException(
    feature: String,
    cause: Throwable? = null,
) : Exception("OPFS feature not available: $feature", cause)
