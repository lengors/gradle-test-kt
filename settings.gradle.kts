pluginManagement {
    val foojayResolverConventionVersion: String by settings
    val kotlinVersion: String by settings
    val ktlintVersion: String by settings
    val koverVersion: String by settings
    val dokkaVersion: String by settings
    val nyxVersion: String by settings

    plugins {
        // Apply the foojay-resolver plugin to allow automatic download of JDKs
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolverConventionVersion
        kotlin("jvm") version kotlinVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
        id("com.mooltiverse.oss.nyx") version nyxVersion
        id("org.jetbrains.dokka") version dokkaVersion
    }
}

rootProject.name = "gradle-test-kt"
