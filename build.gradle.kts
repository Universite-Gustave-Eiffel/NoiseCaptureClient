import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    kotlin("android") version libs.versions.kotlin.get() apply false
    kotlin("multiplatform") version libs.versions.kotlin.get() apply false
    id("com.android.application") version libs.versions.agp.get() apply false
    id("org.jetbrains.compose") version libs.versions.compose.plugin.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

repositories {
    mavenCentral()
}


allprojects {

    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        allRules = true
        config.setFrom("$projectDir/../config/detekt.yml")
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(true)
        }
    }

    // Kotlin DSL
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }
}