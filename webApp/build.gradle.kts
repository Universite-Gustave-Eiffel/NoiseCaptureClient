import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        moduleName = "appyx-starter-kit-web"
        browser()
        binaries.executable()
    }
    sourceSets {
        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(project(":shared"))
                implementation(libs.appyx.navigation)
                implementation(libs.appyx.components.backstack)
                implementation(libs.koin.core)
            }
        }
    }
}

compose.experimental {
    web.application {}
}
