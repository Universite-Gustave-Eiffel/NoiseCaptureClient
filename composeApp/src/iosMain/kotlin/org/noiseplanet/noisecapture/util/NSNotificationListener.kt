package org.noiseplanet.noisecapture.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import platform.darwin.NSObject
import platform.objc.sel_registerName


/**
 * A utility wrapper for adding/removing observers to [NSNotificationCenter].
 *
 * Since handling of Obj-C selectors in Kotlin can be a bit tricky (enclosing class must inherit
 * [NSObject] and this comes with restrictions like not being able to use companion object or
 * inherit from another Kotlin type), this class provides a way to encapsulate [NSNotification]
 * subscription by providing only the required parameter and a callback method that will be
 * called with the [NSNotification] object when a notification is received.
 *
 * @param notificationName Name of the target notification. Should not be a hardcoded string but
 *                         rather one of the provided enums by Obj-C interop.
 * @param object Object parameter that needs to be passed down for certain notifications.
 *               See the apple docs for the desired notification for more details.
 * @param callback A callback lambda that will be called with the received [NSNotification] object.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class NSNotificationListener(
    private val notificationName: NSNotificationName,
    private val `object`: Any? = null,
    private val callback: (NSNotification) -> Unit,
) : NSObject() {

    /**
     * Starts listening to the current [NSNotificationName].
     * Internally calls [NSNotificationCenter.addObserver].
     */
    fun startListening() {
        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = sel_registerName("${::handleNotification.name}:"),
            name = notificationName,
            `object` = `object`
        )
    }

    /**
     * Stops listening to the current [NSNotificationName].
     * Internally calls [NSNotificationCenter.removeObserver]
     */
    fun stopListening() {
        NSNotificationCenter.defaultCenter.removeObserver(this)
    }

    /**
     * Upon de-init, remove any subscribed observer.
     */
    override fun finalize() {
        stopListening()
        super.finalize()
    }

    /**
     * Obj-C selector that will be called whenever a notification is received.
     * Internally passes down the notification to the provided callback.
     */
    @ObjCAction
    private fun handleNotification(notification: NSNotification) {
        callback(notification)
    }
}
