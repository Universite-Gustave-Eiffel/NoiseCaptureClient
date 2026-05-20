import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
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

// Read values from version.properties and set them in rootProject.extra to be retrieved in submodules
val versionProperties = Properties().apply {
    val versionFile = project.rootProject.file("version.properties")
    if (versionFile.exists()) {
        load(versionFile.inputStream())
    }
}
extra.set("versionName", versionProperties.getProperty("versionName"))
extra.set("versionCode", versionProperties.getProperty("versionCode"))
