plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.target.get()
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "permissions"
            isStatic = true
        }
    }

    js(IR) {
        // Adding moduleName as a workaround for this issue: https://youtrack.jetbrains.com/issue/KT-51942
        moduleName = "permissions-common"
        browser()
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.appcompat)
                implementation(libs.kotlin.coroutines.core)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlin.browser)
            }
        }
    }
}

android {
    namespace = "com.adrianwitaszak.kmmpermissions"
    compileSdk = 34
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
}
