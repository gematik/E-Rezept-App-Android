import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
}

dependencies {
    implementation("com.android.tools.build:gradle:8.1.2")
}

gradlePlugin {
    plugins.register("TechnicalRequirementsPlugin") {
        id = "de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin"
        implementationClass = "de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin"
    }
}
