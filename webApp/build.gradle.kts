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
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(project(":shared"))
                implementation(libs.appyx.navigation)
                implementation(libs.appyx.components.backstack)
            }
        }
    }
}

compose.experimental {
    web.application {}
}
