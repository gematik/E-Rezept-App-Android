plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.di)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.napier)
    implementation(libs.di)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
}
