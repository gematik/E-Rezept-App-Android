plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
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
    implementation(project(":utils"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.di)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.napier)
    implementation(libs.di)
    implementation(libs.classgraph)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.bundles.corutinestest)
    testImplementation(libs.bundles.kotlintest)
    testImplementation(libs.test.junit)
}

configurations
    .matching { it.name.contains("dokka", ignoreCase = true) }
    .all {
        resolutionStrategy {
            force(libs.jackson.core.get())
            force(libs.woodstox.core.get())
        }
    }

val generateFhirUml by tasks.registering(JavaExec::class) {
    group = "documentation"
    description = "Generates FHIR class documentation in Markdown format"

    // Make sure it waits for the main sources to compile
    dependsOn("classes")

    // Correct classpath for compiled classes
    classpath = sourceSets["main"].runtimeClasspath
    println(classpath)

    // Your main class
    mainClass.set("docgen.FhirMarkdownGenerator")

    // Arguments
    val outputFile = file("${buildDir}/dokka/FHIR_Documentation.md")
    val basePackage = "de.gematik.fhir.parser"

    args(outputFile.absolutePath, basePackage)

    doFirst {
        outputFile.parentFile.mkdirs()
    }
}

tasks.dokkaHtml {
    dokkaSourceSets.configureEach {
        includeNonPublic.set(true)
        skipEmptyPackages.set(false)
        reportUndocumented.set(true)
        documentedVisibilities.set(
            setOf(
                org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC,
                org.jetbrains.dokka.DokkaConfiguration.Visibility.INTERNAL
            )
        )
    }
}
