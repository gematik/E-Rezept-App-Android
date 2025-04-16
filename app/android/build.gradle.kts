import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin
import java.util.Properties

plugins {
    id("base-android-application")
    id("de.gematik.ti.erp.dependency-overrides")
    id("de.gematik.ti.erp.names")
    // Release app into play-store
    id("com.github.triplet.play") version "3.8.6" apply true
    alias(libs.plugins.compose.compiler)
}

// these two need to be in uppercase since it is declared that way in gradle.properties
@Suppress("VariableNaming", "PropertyName")
val VERSION_CODE: String by overrides()
@Suppress("VariableNaming", "PropertyName")
val VERSION_NAME: String by overrides()
val gematik = AppDependencyNamesPlugin()
val isRunningOnJenkins = System.getenv("JENKINS_HOME") != null // Check if running on Jenkins
val googleRelease = "googleRelease"
val huaweiRelease = "huaweiRelease"

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

    println("Running on Jenkins: $isRunningOnJenkins")

    val rootProject = project.rootProject

    // Load the signing properties from environment variable or local file
    val signingProperties = rootProject.getSigningProperties()

    if (!signingProperties.isNullOrEmpty()) {
        signingConfigs {
            fun createRelease() = creating {
                try {
                    val target = this.name
                    println("BuildGradle: Create signing config for: $target")
                    storeFile = when (isRunningOnJenkins) {
                        true -> {
                            if (target == googleRelease) {
                                println("BuildGradle: Google release ${System.getenv("KEYSTORE_PLAY_PATH")}")
                                rootProject.file(System.getenv("KEYSTORE_PLAY_PATH"))
                            } else if (target == huaweiRelease) {
                                println("BuildGradle: Huawei release ${System.getenv("KEYSTORE_HUAWEI_PATH")}")
                                rootProject.file(System.getenv("KEYSTORE_HUAWEI_PATH"))
                            } else {
                                signingProperties["$target.storePath"]?.let { rootProject.file("erp-app-android/$it") }
                            }
                        }

                        false -> signingProperties["$target.storePath"]?.let { rootProject.file("erp-app-android/$it") }

                    }
                    keyAlias = signingProperties["$target.keyAlias"] as? String
                    storePassword = signingProperties["$target.storePassword"] as? String
                    keyPassword = signingProperties["$target.keyPassword"] as? String
                } catch (e: Exception) {
                    println("BuildGradle: Error creating signing configs: ${e.stackTraceToString()}")
                }
            }

            // Create the signing config based on the properties found
            when {
                signingProperties["${googleRelease}.storePath"] != null -> {
                    val googleRelease by createRelease()
                }

                signingProperties["${huaweiRelease}.storePath"] != null -> {
                    val huaweiRelease by createRelease()
                }

                else -> {
                    println("BuildGradle: No google or huawei release signing properties found!")
                }
            }
        }
    } else {
        println("BuildGradle: No signing properties found!")
    }

    buildTypes {
        // need to declare the val for buildType to be recognized
        @Suppress("unused")
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (!signingProperties.isNullOrEmpty()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept")
        }
        // need to declare the val for buildType to be recognized
        @Suppress("unused")
        val debug by getting {
            applicationIdSuffix = ".test"
            versionNameSuffix = "-debug"
            resValue("string", "app_label", "E-Rezept Debug")
            if (rootProject.file("keystore/debug.keystore").exists()) { // needed tp be able to build on github
                signingConfigs {
                    getByName("debug") {
                        storeFile = rootProject.file("keystore/debug.keystore")
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
            if (!signingProperties.isNullOrEmpty()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept Mini")
        }
    }
    flavorDimensions += listOf("version")
    productFlavors {
        val flavor = project.findProperty("buildkonfig.flavor") as? String
        if (flavor?.startsWith("google") == true) {
            try {
                create(flavor) {
                    dimension = "version"
                    signingConfig = signingConfigs.findByName(googleRelease)
                }
            } catch (e: Exception) {
                println("BuildGradle: Error creating google flavor: ${e.stackTraceToString()}")
            }

        }
        if (flavor?.startsWith("huawei") == true) {
            try {
                create(flavor) {
                    dimension = "version"
                    applicationIdSuffix = ".huawei"
                    versionNameSuffix = "-huawei"
                    signingConfig = signingConfigs.findByName(huaweiRelease)
                }
            } catch (e: Exception) {
                println("BuildGradle: Error creating huawei flavor: ${e.stackTraceToString()}")
            }

        }
        if (flavor?.startsWith("konnektathonRu") == true) {
            try {
                create(flavor) {
                    dimension = "version"
                    applicationIdSuffix = ".konnektathon.ru"
                    versionNameSuffix = "-konnektathon-RU"
                    signingConfig = signingConfigs.findByName(googleRelease)
                    resValue("string", "app_label", "E-Rezept Konny")
                }
            } catch (e: Exception) {
                println("BuildGradle: Error creating konnektathonRu flavor: ${e.stackTraceToString()}")
            }

        }
        if (flavor?.startsWith("konnektathonDevru") == true) {
            try {
                create(flavor) {
                    dimension = "version"
                    applicationIdSuffix = ".konnektathon.rudev"
                    versionNameSuffix = "-konnektathon-RUDEV"
                    signingConfig = signingConfigs.findByName(googleRelease)
                    resValue("string", "app_label", "E-Rezept Konny Dev")
                }
            } catch (e: Exception) {
                println("BuildGradle: Error creating konnektathonDevru flavor: ${e.stackTraceToString()}")
            }
        }
    }

    testOptions.animationsDisabled = true
}

dependencies {
    implementation(project(":utils"))
    implementation(project(gematik.feature))
    implementation(project(gematik.demoMode))
    implementation(project(gematik.uiComponents))
    androidTestImplementation(project(gematik.testActions))
    androidTestImplementation(project(gematik.testTags))
    implementation(project(gematik.multiplatform))
    testImplementation(project(gematik.multiplatform))
    testImplementation(project(gematik.fhirParser))
    androidTestImplementation(project(gematik.fhirParser))
    implementation(libs.play.app.update)
    implementation(libs.tracing)
    debugImplementation(libs.tracing)

    androidTestImplementation(libs.kodeon.core)
    androidTestImplementation(libs.kodeon.android)
    androidTestImplementation(libs.primsys.client)
}

/**
 * Loads the signing properties from an environment variable or a local file.
 *
 * @return Properties object containing the signing properties, or null if no properties are found.
 */
fun Project.getSigningProperties(): Properties? {
    val signingProps = Properties()

    // Retrieve signing properties data from the environment variable
    val envPropsData = System.getenv("SIGNING_PROPS_DATA")
    // Define the local signing properties file
    val signingPropsFile: File = this.file("signing.properties")

    // Check if the environment variable is set and not blank
    if (envPropsData != null && envPropsData.isNotBlank()) {
        val envFile = File(envPropsData)
        // Load signing properties from the environment variable
        println("BuildGradle: Loading signing properties from environment variable SIGNING_PROPS_DATA")
        signingProps.load(envFile.inputStream())
        return signingProps
    } else if (signingPropsFile.canRead()) {
        // Load signing properties from the local file
        println("BuildGradle: Signing properties found: $signingPropsFile")
        signingProps.load(signingPropsFile.inputStream())
        return signingProps
    } else {
        // No signing properties found
        println("BuildGradle: No signing properties found!")
        return null
    }

}
