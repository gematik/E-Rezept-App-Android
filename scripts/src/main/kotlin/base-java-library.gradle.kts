import extensions.BuildNames.versionCatalogLibrary
import extensions.Versions.JavaVersion.PROJECT_JAVA_VERSION
import generated.corutinestestBundle
import generated.diLibrary
import generated.kotlintestBundle
import generated.kotlinxCoroutinesCoreLibrary
import generated.kotlinxDatetimeLibrary
import generated.kotlinxSerializationJsonLibrary
import generated.loggingNapierLibrary
import generated.testJunitLibrary
import generated.testMockkLibrary

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}
java {
    sourceCompatibility = PROJECT_JAVA_VERSION
    targetCompatibility = PROJECT_JAVA_VERSION
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

val versionCatalog: VersionCatalog =
    extensions.getByType<VersionCatalogsExtension>().named(versionCatalogLibrary)


dependencies {
    implementation(versionCatalog.kotlinxSerializationJsonLibrary)
    implementation(versionCatalog.kotlinxDatetimeLibrary)
    implementation(versionCatalog.diLibrary)
    implementation(versionCatalog.kotlinxCoroutinesCoreLibrary)
    implementation(versionCatalog.loggingNapierLibrary)

    testImplementation(versionCatalog.testJunitLibrary)
    testImplementation(versionCatalog.testMockkLibrary)
    testImplementation(versionCatalog.corutinestestBundle)
    testImplementation(versionCatalog.kotlintestBundle)
}

