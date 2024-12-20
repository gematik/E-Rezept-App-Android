import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
}

repositories {
    google()
    mavenCentral()
}

version = 1.0
group = "de.gematik.ti.erp.app"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.withType<Test> {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.quality.ktlint.core)
    testImplementation(kotlin("test"))
    testImplementation(libs.test.junit)
    testImplementation(libs.quality.ktlint.test)
}
