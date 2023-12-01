import java.awt.SystemColor.desktop

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.target.get()
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.target.get()
        }
    }

    js(IR) {
        // Adding moduleName as a workaround for this issue: https://youtrack.jetbrains.com/issue/KT-51942
        moduleName = "appyx-starter-kit-common"
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                implementation(kotlin("test"))
                implementation(libs.appyx.navigation)
                api(libs.appyx.components.backstack)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "org.noise_planet.noisecapturekmp.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.appyx.mutable.ui.processor)
    add("kspAndroid", libs.appyx.mutable.ui.processor)
    add("kspDesktop", libs.appyx.mutable.ui.processor)
    add("kspJs", libs.appyx.mutable.ui.processor)
    add("kspIosArm64", libs.appyx.mutable.ui.processor)
    add("kspIosX64", libs.appyx.mutable.ui.processor)
    add("kspIosSimulatorArm64", libs.appyx.mutable.ui.processor)
}
