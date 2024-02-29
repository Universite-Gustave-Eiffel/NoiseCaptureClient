plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
}

kotlin {

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "appyx-starter-kit iOS module"
        homepage = "https://bumble-tech.github.io/appyx/"
        ios.deploymentTarget = "16.4"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ios"
            isStatic = true
        }
        license = "Apache License, Version 2.0"
        authors = "https://github.com/bumble-tech/"
    }

    sourceSets {
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(project(":shared"))
                implementation(libs.appyx.navigation)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }
}

