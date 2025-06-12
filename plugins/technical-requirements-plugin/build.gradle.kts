@file:Suppress("UseTomlInstead", "MagicNumber")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:8.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.11.0")
    implementation("org.jsoup:jsoup:1.17.2")
}

gradlePlugin {
    plugins.register("TechnicalRequirementsPlugin") {
        id = "de.gematik.ti.erp.technical-requirements"
        implementationClass = "de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin"
    }
}
