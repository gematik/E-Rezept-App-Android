@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.BaseExtension
import extensions.BuildNames.implementation
import extensions.BuildNames.versionCatalogLibrary
import generated.androidxComposeBomLibrary
import generated.composeLibsWithBomBundle
import generated.composeLibsWithoutBomBundle

plugins {
    id("com.android.base")
    id("org.jetbrains.compose")
}

configure<BaseExtension> {
    buildFeatures.compose = true
}
val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>()
    .named(versionCatalogLibrary)

dependencies {
    implementation(platform(versionCatalog.androidxComposeBomLibrary))
    implementation(versionCatalog.composeLibsWithBomBundle)
    implementation(versionCatalog.composeLibsWithoutBomBundle)
}

