import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("android") version libs.versions.kotlin.get() apply false
    kotlin("multiplatform") version libs.versions.kotlin.get() apply false
    id("com.android.application") version libs.versions.agp.get() apply false
    id("org.jetbrains.compose") version libs.versions.compose.plugin.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("io.gitlab.arturbosch.detekt") version libs.versions.detekt.get()
}

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        allRules = true
        config.setFrom("$rootDir/config/detekt.yml")
        source.setFrom(
            "src/main/kotlin",
            "src/iosMain/kotlin",
            "src/androidMain/kotlin",
            "src/jsMain/kotlin",
            "src/commonMain/kotlin",
        )
        autoCorrect = true
    }

//    dependencies {
//        detektPlugins(rootProject.libs.detekt.formatting)
//    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = libs.versions.jvm.target.get()
        reports {
            html.required.set(true)
        }
        basePath = rootDir.absolutePath
    }
}
