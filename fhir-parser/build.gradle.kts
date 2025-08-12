plugins {
    alias(libs.plugins.base.java.library)
}

dependencies {
    implementation(project(":erp-model"))
    implementation(project(":utils"))
}
