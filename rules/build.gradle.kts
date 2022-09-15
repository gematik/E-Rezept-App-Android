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

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.pinterest.ktlint:ktlint-core:0.43.2")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.pinterest.ktlint:ktlint-test:0.43.2")
}
