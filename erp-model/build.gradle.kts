plugins {
    alias(libs.plugins.base.java.library)
}

dependencies {
    implementation(project(":utils"))
    implementation(libs.bundles.crypto)
}

// NOTE: This is the data layer module which is used in other feature modules
