package org.noiseplanet.noisecapture.model.dao

/**
 * To reduce the size of JSON exports when storing sequences of objects (like [LeqRecord] or [LocationRecord])
 * we will group values of the same property in a list to avoid duplicating keys when serializing.
 *
 * Each record fragment is then stored in its separate JSON file when serialising.
 *
 * **Example**:
 * ```kotlin
 * class Record {
 *      val timestamp: Long
 *      val recordValue: Double
 * }
 * val recordSequence: List<Record>
 *
 * // Would become
 * class RecordSequenceFragment: SequenceFragment<Record> {
 *      val timestamps: List<Long>
 *      val recordValue: List<Double>
 *
 *      fun push(element: Record) {
 *          timestamps.add(element.timestamp)
 *          recordValue.add(element.recordValue)
 *      }
 * }
 * val recordSequence: List<RecordSequenceFragment>
 * ```
 */
interface SequenceFragment<T> {

    // - Properties

    /**
     * Unique identifier.
     */
    val uuid: String

    /**
     * Index of this fragment in the sequence, starting from 0.
     */
    val index: Int

    /**
     * UUID of the measurement this sequence of values is attached to.
     */
    val measurementId: String

    /**
     * Timestamps of each record in the sequence.
     * In milliseconds since epoch (UTC)
     */
    val timestamp: List<Long>

    /**
     * Start timestamp of the sequence fragment.
     */
    val startTimestamp: Long?
        get() = timestamp.minOrNull()

    /**
     * End timestamp of the sequence fragment.
     */
    val endTimestamp: Long?
        get() = timestamp.maxOrNull()

    /**
     * Duration of the sequence fragment.
     */
    val duration: Long?
        get() = endTimestamp?.let { startTimestamp?.minus(it) }

    /**
     * Number of elements in the sequence.
     */
    val size: Int
        get() = timestamp.size


    // - Public unctions

    /**
     * Pushes a new element to the sequence.
     */
    fun push(element: T)
}
