import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.5.31"
    `java-gradle-plugin`
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

gradlePlugin {
    plugins.register("resgen") {
        id = "de.gematik.ti.erp.resgen"
        implementationClass = "de.gematik.ti.erp.ResourceGenerationPlugin"
    }
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.10.1")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.83.0")
    implementation("io.github.pdvrieze.xmlutil:core-jvm:0.83.0")
}
