plugins {
    kotlin("android") version libs.versions.kotlin.get() apply false
    kotlin("multiplatform") version libs.versions.kotlin.get() apply false
    id("com.android.application") version libs.versions.agp.get() apply false
    id("org.jetbrains.compose") version libs.versions.compose.plugin.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
}
