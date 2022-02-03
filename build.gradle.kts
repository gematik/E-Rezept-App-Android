// NOTE: Only pre-include plugins (apply false) required by the modules android, common
// and desktop within this block to keep them excluded from the root module.
// If the plugin can't be resolved add a custom resolution strategy to `settings.gradle.kts`.
plugins {
    // reports versions of dependencies
    // e.g. `gradle dependencyUpdates`
    id("com.github.ben-manes.versions") version "0.41.0"

    id("org.owasp.dependencycheck") version "6.5.2.1" apply false

    // generates licence report
    id("com.jaredsburrows.license") version "0.8.90" apply false

    kotlin("multiplatform") version "1.6.10" apply false
    kotlin("plugin.serialization") version "1.6.10" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
    id("com.android.application") version "7.0.4" apply false
    id("com.android.library") version "7.0.4" apply false
    id("dagger.hilt.android") version "2.40.5" apply false
    id("org.jetbrains.compose") version "1.0.1" apply false
    id("com.codingfeline.buildkonfig") version "0.11.0" apply false
}

// BUG: Workaorund for missing metadata https://issuetracker.google.com/issues/206855609
// TODO: Remove if we can upgrade to AGP >= 7.1.*
buildscript {
    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
        repositories {
            maven("https://storage.googleapis.com/r8-releases/raw")
        }
        dependencies {
            classpath("com.android.tools:r8:3.1.42")
        }
    }
}
// END

val ktlintMain by configurations.creating
val ktlintRules by configurations.creating

dependencies {
    ktlintMain("com.pinterest:ktlint:0.42.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
    }
    ktlintRules("de.gematik.ti.erp.app:rules:1.0")
}

val sourcesKt = listOf(
    "android/src/**/de/gematik/**/*.kt",
    "common/src/**/de/gematik/**/*.kt",
    "desktop/src/**/de/gematik/**/*.kt",
    "rules/src/**/de/gematik/**/*.kt",
    "smartcard-wrapper/src/**/de/gematik/**/*.kt",
    "plugins/*/src/**/*.kt",

    "**/*.gradle.kts"
)

fun ktlintCreating(format: Boolean, sources: List<String>, disableLicenceRule: Boolean) =
    tasks.creating(JavaExec::class) {
        description = "Fix Kotlin code style deviations."
        classpath = ktlintMain + ktlintRules
        main = "com.pinterest.ktlint.Main"
        args = mutableListOf<String>().apply {
            if (format) add("-F")
            addAll(sources)
            if (disableLicenceRule) add("--disabled_rules=custom:licence-header")
        }
    }

val ktlint by ktlintCreating(format = false, sources = sourcesKt, disableLicenceRule = false)
val ktlintFormat by ktlintCreating(format = true, sources = sourcesKt, disableLicenceRule = false)

tasks.register("clean", Delete::class) {
    rootProject.allprojects.forEach {
        delete(it.buildDir)
    }
}
