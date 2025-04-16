@file:Suppress("SpreadOperator")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.Properties

private val ktlintMain: Configuration by configurations.creating
private val ktlintRules: Configuration by configurations.creating

plugins {

    // module plugins
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

    // extra plugins
    alias(libs.plugins.github.ben.manes.version) apply false
    /** todo issue to be fixed:
     *
     *    > Could not resolve all task dependencies for configuration ':classpath'.
     *       > Could not find org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2.
     *         Required by:
     *             project : > com.jaredsburrows.license:com.jaredsburrows.license.gradle.plugin:0.8.90 > com.jaredsburrows:gradle-license-plugin:0.8.90
     */
    // alias(libs.plugins.jaredburrows.license) apply false
    alias(libs.plugins.gradle.secrets) apply false
    alias(libs.plugins.realm.kotlin) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt)

    alias(libs.plugins.compose.compiler) apply false

    // test
    alias(libs.plugins.paparazzi) apply false
    id("jacoco")

    // custom gematik plugins
    id("de.gematik.ti.erp.technical-requirements")
    id("de.gematik.ti.erp.versioning")
    id("de.gematik.ti.erp.properties")
    id("de.gematik.ti.erp.flavours")
    id("de.gematik.ti.erp.teams")
    id("de.gematik.ti.erp.names")
}

// obtain libs from nexus
try {
    val properties = Properties()
    properties.load(File("ci-overrides.properties").inputStream())
    val nexusUsername: String? = properties.getProperty("NEXUS_USERNAME")
    val nexusPassword: String? = properties.getProperty("NEXUS_PASSWORD")
    val nexusUrl: String? = properties.getProperty("NEXUS_URL")

    allprojects {
        repositories {
            if (!nexusUsername.isNullOrEmpty() && !nexusPassword.isNullOrEmpty() && !nexusUrl.isNullOrEmpty()) {
                maven {
                    name = "nexus"
                    setUrl(nexusUrl)
                    credentials {
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            }
        }
    }
} catch (e: Throwable) {
    println("No ci-overrides.properties found")
}

val sourcesKt = listOf(
    "app/android/src/**/*.kt",
    "app/android-mock/src/**/*.kt",
    "app/demo-mode/src/**/*.kt",
    "app/features/src/**/*.kt",
    "app/digas/src/**/*.kt",
    "app/navigation/src/**/*.kt",
    "app/test-actions/src/**/*.kt",
    "app/test-tags/src/**/*.kt",
    "app/fhir-vzd/src/**/*.kt",
    "buildSrc/src/**/*.kt",
    "common/src/**/*.kt",
    "desktop/src/**/*.kt",
    "plugins/*/src/**/*.kt",
    "rules/src/**/*.kt",
    "scripts/src/**/*.kt",
    "smartcard-wrapper/src/**/*.kt",
    "ui-components/src/**/*.kt",
    "fhir-parser/src/**/*.kt"
)

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

// config changes for security vulnerabilities
allprojects {
    configurations.all {
        resolutionStrategy {
            force("io.netty:netty-codec-http2:4.1.108.Final")
            force("io.netty:netty-handler:4.1.118.Final")
            force("com.google.protobuf:protobuf-java:4.28.2")
            force("com.google.guava:guava:33.2.0-jre")
        }
    }
}
