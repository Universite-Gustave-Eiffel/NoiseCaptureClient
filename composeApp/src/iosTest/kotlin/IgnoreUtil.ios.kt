import kotlin.test.Ignore

/**
 * Alias for [kotlin.test.Ignore]
 */
actual typealias IgnoreIos = Ignore

/**
 * Doesn't do anything
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreAndroid
