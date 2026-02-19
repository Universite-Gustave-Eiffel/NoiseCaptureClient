import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.buildkonfig.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildKonfigGradlePlugin)
}

val appNamespace: String by project
val appVersionCode: String by project
val appVersionName: String by project

buildkonfig {
    packageName = appNamespace

    defaultConfigs {
        buildConfigField(Type.STRING, name = "versionName", value = appVersionName, const = true)
        buildConfigField(Type.INT, name = "versionCode", value = appVersionCode)
    }
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    androidLibrary {
        namespace = "org.noiseplanet.noisecapture.library"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        iosTarget.compilations.getByName("main") {
            cinterops.create("DeviceUtil") {
                definitionFile.set(file(rootDir.absolutePath + "/iosApp/iosApp/DeviceUtil.def"))
                includeDirs.allHeaders(rootDir.absolutePath + "/iosApp/iosApp/")
            }
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.navigation)
            implementation(libs.compose.navigationevent)
            implementation(libs.compose.lifecycle.viewmodel)
            implementation(libs.compose.lifecycle.runtime)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)

            implementation(libs.koalaplot.core)
            implementation(libs.kstore)
            implementation(libs.ktor.client.core)
            implementation(libs.humanreadable)
            implementation(libs.maps.compose)

            implementation(libs.settings.multiplatform)
            implementation(libs.settings.multiplatform.serialization)
            implementation(libs.settings.multiplatform.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.androidx.preference)
            implementation(libs.google.play.services.android.location)
            implementation(libs.kstore.file)
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            implementation(libs.kstore.file)
            implementation(libs.ktor.client.darwin)
        }

        wasmJsMain.dependencies {
            implementation(libs.kstore.storage)
            implementation(npm("@zip.js/zip.js", libs.versions.zipjs.get()))
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.compose.components.resources)
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
