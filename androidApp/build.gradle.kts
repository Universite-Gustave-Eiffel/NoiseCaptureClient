import org.gradle.internal.extensions.core.extra
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    dependencies {
        implementation(projects.composeApp)

        implementation(libs.compose.material3.adaptive)
        implementation(libs.androidx.activity.compose)
        implementation(libs.koin.android)
    }

    val appPackageName: String by project
    val androidAppId: String by project
    val appVersionName = rootProject.extra.get("versionName") as String
    val appVersionCode = rootProject.extra.get("versionCode") as String

    android {
        namespace = appPackageName
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            applicationId = androidAppId
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            versionCode = appVersionCode.toInt()
            versionName = appVersionName
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        buildFeatures {
            compose = true
        }
    }
}
