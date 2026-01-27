@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

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
