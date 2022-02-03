import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

gradlePlugin {
    plugins.register("dependencies") {
        id = "de.gematik.ti.erp.dependencies"
        implementationClass = "de.gematik.ti.erp.AppDependenciesPlugin"
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.3")
}
