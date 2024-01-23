import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// NOTE: Only pre-include plugins (apply false) required by the modules android, common
// and desktop within this block to keep them excluded from the root module.
// If the plugin can't be resolved add a custom resolution strategy to `settings.gradle.kts`.
plugins {

    // Running `./gradlew dependencyUpdates` looks for the latest libs that are available
    id("com.github.ben-manes.versions") version "0.48.0"

    id("org.owasp.dependencycheck") version "8.0.2" apply false

    // generates licence report
    id("com.jaredsburrows.license") version "0.8.90" apply false

    id("com.android.application") version "8.1.0" apply false

    id("com.android.library") version "8.1.0" apply false

    kotlin("multiplatform") version "1.9.10" apply false

    kotlin("plugin.serialization") version "1.8.10" apply false

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false

    id("io.realm.kotlin") version "1.7.1" apply false

    // TODO: Update to latest version : https://github.com/JetBrains/compose-multiplatform/blob/master/VERSIONING.md
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false

    id("org.jetbrains.compose") version "1.5.3" apply false

    id("com.codingfeline.buildkonfig") version "0.13.3" apply false

    id("io.gitlab.arturbosch.detekt") version "1.22.0"

    id("de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin")
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false

}

val ktlintMain: Configuration by configurations.creating
val ktlintRules: Configuration by configurations.creating

dependencies {
    ktlintMain("com.pinterest:ktlint:0.46.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
    }
    ktlintRules("de.gematik.ti.erp.app:rules:1.0")
}

val sourcesKt = listOf(
    "app/android/src/**/de/gematik/**/*.kt",
    "app/features/src/**/de/gematik/**/*.kt",
    "common/src/**/de/gematik/**/*.kt",
    "desktop/src/**/de/gematik/**/*.kt",
    "rules/src/**/de/gematik/**/*.kt",
    "smartcard-wrapper/src/**/de/gematik/**/*.kt",
    "plugins/*/src/**/*.kt",

    "**/*.gradle.kts"
)

detekt {
    source =
        fileTree(rootDir) {
            include(sourcesKt)
        }
            .filter { it.extension != "kts" }
            .map { it.parentFile }
            .let {
                files(*it.toTypedArray())
            }
    parallel = true
    config = files("config/detekt/detekt.yml")
    baseline = file("config/detekt/baseline.xml")
    buildUponDefaultConfig = false
    allRules = false
    disableDefaultRuleSets = false
    debug = false
    ignoreFailures = false
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
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    }

val ktlint by ktlintCreating(format = false, sources = sourcesKt, disableLicenceRule = false)
val ktlintFormat by ktlintCreating(format = true, sources = sourcesKt, disableLicenceRule = false)

tasks.register("clean", Delete::class) {
    rootProject.allprojects.forEach {
        delete(it.buildDir)
    }
}

fun isUnstable(version: String): Boolean =
    version.contains("alpha", ignoreCase = true)
        || version.contains("rc", ignoreCase = true)
        || version.contains("beta", ignoreCase = true)

tasks.withType<DependencyUpdatesTask> {
    outputFormatter = "txt,html"
    rejectVersionIf {
        // allows unstable to unstable updates but not stable to unstable
        isUnstable(candidate.version) && !isUnstable(currentVersion)
    }
}
