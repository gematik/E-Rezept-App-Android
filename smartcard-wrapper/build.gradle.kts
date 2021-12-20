import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
}

repositories {
    google()
    mavenCentral()
}

version = 1.0
group = "de.gematik.ti.erp.app"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

dependencies {
    implementation(kotlin("stdlib"))
}
