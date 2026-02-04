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

    val appNamespace: String by project
    val appPackageName: String by project
    val appVersionCode: String by project
    val appVersionName: String by project

    android {
        namespace = appNamespace
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            applicationId = appPackageName
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
