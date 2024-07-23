plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("app.cash.sqldelight") version "2.0.1"
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.target.get()
        }
    }

    js(IR) {
        // Adding moduleName as a workaround for this issue: https://youtrack.jetbrains.com/issue/KT-51942
        moduleName = "noisecapture-common"
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(projects.permissions)
                implementation(libs.kotlinx.datetime)
                api(compose.runtime)
                api(compose.foundation)
                implementation(libs.appyx.navigation)
                api(libs.appyx.components.backstack)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(libs.koin.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
        val jsMain by getting() {
            dependencies {
                implementation(libs.kotlin.browser)
                implementation(libs.sqldelight.driver)
                implementation(npm("sql.js", "1.6.2"))
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            }
        }
        val androidMain by getting() {
            dependencies {
                implementation(libs.sqldelight.android.driver)
                implementation(libs.androidx.sqlite.framework)
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            multiDexEnabled = true
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.appyx.processor)
    add("kspAndroid", libs.appyx.processor)
    add("kspJs", libs.appyx.processor)
    add("kspIosArm64", libs.appyx.processor)
    add("kspIosX64", libs.appyx.processor)
    add("kspIosSimulatorArm64", libs.appyx.processor)
}

sqldelight {
    databases {
        create("Storage") {
            packageName.set("org.noise_planet.noisecapture")
        }
    }
}