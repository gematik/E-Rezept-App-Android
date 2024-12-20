@file:Suppress("VariableNaming", "PropertyName", "UnusedPrivateProperty")

import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin
import java.util.Properties

plugins {
    id("base-android-application")
    id("de.gematik.ti.erp.dependency-overrides")
    id("de.gematik.ti.erp.names")
    // Release app into play-store
    id("com.github.triplet.play") version "3.8.6" apply true
}

// these two need to be in uppercase since it is declared that way in gradle.properties
val VERSION_CODE: String by overrides()
val VERSION_NAME: String by overrides()
val gematik = AppDependencyNamesPlugin()

android {
    namespace = gematik.appNameSpace
    defaultConfig {
        applicationId = gematik.appId
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = gematik.moduleName("test.test")
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        testOptions.execution = "ANDROID_TEST_ORCHESTRATOR"
        // Check if MAPS_API_KEY is defined, otherwise provide a default value
        val mapsApiKey = project.findProperty("MAPS_API_KEY") ?: "DEFAULT_PLACEHOLDER_KEY"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    val rootPath = project.rootProject
    val signingPropsFile = rootPath.file("signing.properties")
    if (signingPropsFile.canRead()) {
        println("Signing properties found: $signingPropsFile")
        val signingProps = Properties()
        signingProps.load(signingPropsFile.inputStream())
        signingConfigs {
            fun creatingRelease() =
                creating {
                    val target = this.name // property name; e.g. googleRelease
                    println("Create signing config for: $target")
                    storeFile =
                        signingProps["$target.storePath"]?.let {
                            rootPath.file("erp-app-android/$it")
                        }
                    println("\tstore: ${signingProps["$target.storePath"]}")
                    keyAlias = signingProps["$target.keyAlias"] as? String
                    println("\tkeyAlias: ${signingProps["$target.keyAlias"]}")
                    storePassword = signingProps["$target.storePassword"] as? String
                    keyPassword = signingProps["$target.keyPassword"] as? String
                }
            if (signingProps["googleRelease.storePath"] != null) {
                val googleRelease by creatingRelease()
            }
            if (signingProps["huaweiRelease.storePath"] != null) {
                val huaweiRelease by creatingRelease()
            }
        }
    } else {
        println("No signing properties found!")
    }
    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (signingPropsFile.canRead()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept")
        }
        val debug by getting {
            applicationIdSuffix = ".test"
            versionNameSuffix = "-debug"
            resValue("string", "app_label", "E-Rezept Debug")
            if(rootPath.file("keystore/debug.keystore").exists()) { // needed tp be able to build on github
                signingConfigs {
                    getByName("debug") {
                        storeFile = rootPath.file("keystore/debug.keystore")
                        keyAlias = "androiddebugkey"
                        storePassword = "android"
                        keyPassword = "android"
                    }
                }
            }
        }
        create(gematik.minifiedDebug) {
            applicationIdSuffix = ".minirelease"
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (signingPropsFile.canRead()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept Mini")
        }
    }
    flavorDimensions += listOf("version")
    productFlavors {
        val flavor = project.findProperty("buildkonfig.flavor") as? String
        if (flavor?.startsWith("google") == true) {
            create(flavor) {
                dimension = "version"
                signingConfig = signingConfigs.findByName("googleRelease")
            }
        }
        if (flavor?.startsWith("huawei") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".huawei"
                versionNameSuffix = "-huawei"
                signingConfig = signingConfigs.findByName("huaweiRelease")
            }
        }
        if (flavor?.startsWith("konnektathonRu") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".konnektathon.ru"
                versionNameSuffix = "-konnektathon-RU"
                signingConfig = signingConfigs.findByName("googleRelease")
                resValue("string", "app_label", "E-Rezept Konny")
            }
        }
        if (flavor?.startsWith("konnektathonDevru") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".konnektathon.rudev"
                versionNameSuffix = "-konnektathon-RUDEV"
                signingConfig = signingConfigs.findByName("googleRelease")
                resValue("string", "app_label", "E-Rezept Konny Dev")
            }
        }
    }

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(project(gematik.feature))
    implementation(project(gematik.demoMode))
    implementation(project(gematik.uiComponents))
    androidTestImplementation(project(gematik.testActions))
    androidTestImplementation(project(gematik.testTags))
    implementation(project(gematik.multiplatform))
    testImplementation(project(gematik.multiplatform))
    implementation(libs.play.app.update)
    implementation(libs.tracing)
    debugImplementation(libs.tracing)

    androidTestImplementation(libs.kodeon.core)
    androidTestImplementation(libs.kodeon.android)
    androidTestImplementation(libs.primsys.client)
}

// keep this here since it cannot be changed for mock app
configurations.all {
    resolutionStrategy {
        force("io.netty:netty-codec-http2:4.1.100.Final")
        force("com.google.protobuf:protobuf-java:4.28.2")
    }
}

