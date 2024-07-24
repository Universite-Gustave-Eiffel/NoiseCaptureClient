import org.junit.Ignore

/**
 * Alias for [kotlin.test.Ignore]
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreIos

/**
 * Doesn't do anything
 */
actual typealias IgnoreAndroid = Ignore
