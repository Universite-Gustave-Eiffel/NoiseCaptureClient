@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

/**
 * This annotation will be implemented in iOS sources to ignore a test function or class,
 * but will just be a dummy annotation for Android
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreIos()

/**
 * This annotation will be implemented in Android sources to ignore a test function or class,
 * but will just be a dummy annotation for iOS
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreAndroid
