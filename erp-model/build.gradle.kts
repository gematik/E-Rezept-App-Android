plugins {
    alias(libs.plugins.base.java.library)
}

dependencies {
    implementation(project(":utils"))
}

// NOTE: This is the data layer module which is used in other feature modules
