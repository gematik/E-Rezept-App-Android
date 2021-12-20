// NOTE: Only pre-include plugins (apply false) required by the modules android, common
// and desktop within this block to keep them excluded from the root module.
// If the plugin can't be resolved add a custom resolution strategy to `settings.gradle.kts`.
plugins {
    // reports versions of dependencies
    // e.g. `gradle dependencyUpdates`
    id("com.github.ben-manes.versions") version "0.39.0"

    id("org.owasp.dependencycheck") version "6.3.1" apply false

    // generates licence report
    id("com.jaredsburrows.license") version "0.8.90" apply false

    kotlin("multiplatform") version "1.5.31" apply false
    kotlin("plugin.serialization") version "1.5.31" apply false
    id("org.jetbrains.kotlin.android") version "1.5.31" apply false
    id("com.android.application") version "7.0.3" apply false
    id("com.android.library") version "7.0.3" apply false
    id("dagger.hilt.android") version "2.39.1" apply false
    id("org.jetbrains.compose") version "1.0.0-beta5" apply false
    id("com.codingfeline.buildkonfig") version "0.11.0" apply false
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.42.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("-F", "**/*.kt", "**/*.kts", "*.kt", "*.kts")
}

tasks.register("clean", Delete::class) {
    rootProject.allprojects.forEach {
        delete(it.buildDir)
    }
}
