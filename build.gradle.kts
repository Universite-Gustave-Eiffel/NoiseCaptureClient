import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        allRules = true
        config.setFrom("$rootDir/config/detekt.yml")
        source.setFrom(
            "composeApp"
        )
        autoCorrect = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = JvmTarget.JVM_18.target
        reports {
            html.required.set(true)
        }
        basePath = rootDir.absolutePath
    }
}
