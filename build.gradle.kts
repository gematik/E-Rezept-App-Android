@file:Suppress("SpreadOperator", "unused")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

private val ktlintMain: Configuration by configurations.creating
private val ktlintRules: Configuration by configurations.creating

plugins {

    // module plugins
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

    // required
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // extra plugins
    alias(libs.plugins.github.ben.manes.version) apply false
    alias(libs.plugins.gradle.secrets) apply false
    alias(libs.plugins.realm.kotlin) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.detekt)

    // test
    alias(libs.plugins.paparazzi) apply false
    id("jacoco")

    // custom gematik plugins
    alias(libs.plugins.forced.dependencies)
    alias(libs.plugins.technical.requirements)
    alias(libs.plugins.task.versions)
    alias(libs.plugins.task.properties)
    alias(libs.plugins.task.flavours)
    alias(libs.plugins.teams.communication)
    alias(libs.plugins.module.names)
}

val sourcesKt: List<String> by lazy {
    rootProject.fileTree(rootProject.projectDir) {
        include("**/src/**/*.kt")
    }.files.filter { it.exists() }.map { it.absolutePath }
}

dependencies {
    ktlintMain(libs.quality.ktlint) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
    }
    ktlintRules(libs.quality.rules)
}

fun ktlintCreating(format: Boolean, sources: List<String>, disableLicenceRule: Boolean) =
    tasks.creating(JavaExec::class) {
        description = "Fix Kotlin code style deviations."
        classpath = ktlintMain + ktlintRules
        mainClass.set("com.pinterest.ktlint.Main")
        args = mutableListOf<String>().apply {
            if (format) add("-F")
            addAll(sources)
            if (disableLicenceRule) add("--disabled_rules=custom:licence-header")
        }
        // required for java > 16; see https://github.com/pinterest/ktlint/issues/1195
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED")
        isIgnoreExitValue = false
        standardOutput = System.out
        errorOutput = System.err
    }

val ktlint by ktlintCreating(format = false, sources = sourcesKt, disableLicenceRule = false)
val ktlintFormat by ktlintCreating(format = true, sources = sourcesKt, disableLicenceRule = false)

tasks.register("clean", Delete::class) {
    rootProject.allprojects.forEach {
        delete(it.buildDir)
    }
}

fun isUnstable(version: String): Boolean =
    version.contains("alpha", ignoreCase = true) ||
            version.contains("rc", ignoreCase = true) ||
            version.contains("beta", ignoreCase = true)

tasks.withType<DependencyUpdatesTask> {
    outputFormatter = "txt,html"
    rejectVersionIf {
        // allows unstable to unstable updates but not stable to unstable
        isUnstable(candidate.version) && !isUnstable(currentVersion)
    }
}

tasks.withType<Test> {
    ignoreFailures = false
    maxParallelForks = 1 // Optional
}
