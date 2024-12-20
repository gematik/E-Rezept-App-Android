
plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    alias(libs.plugins.detekt)
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.buildkonfig.gradle.plugin)
    implementation(libs.secrets.gradle.plugin)
    implementation(libs.dependency.check.gradle)
    implementation(libs.gradle.license.plugin)
    implementation(libs.database.realm.plugin)
    implementation(libs.compose.plugin)
    implementation(libs.kotlin.serilization.plugin)
    implementation(libs.quality.detekt)
}
