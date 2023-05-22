import de.gematik.ti.erp.app
import de.gematik.ti.erp.overriding
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    id("io.realm.kotlin")
    id("kotlin-parcelize")
    id("org.owasp.dependencycheck")
    id("com.jaredsburrows.license")
    id("de.gematik.ti.erp.dependencies")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val VERSION_CODE: String by overriding()
val VERSION_NAME: String by overriding()
val TEST_INSTRUMENTATION_ORCHESTRATOR: String? by project

afterEvaluate {
    val taskRegEx = """assemble(Google|Huawei)(PuExternalDebug|PuExternalRelease)""".toRegex()
    tasks.forEach { task ->
        taskRegEx.matchEntire(task.name)?.let {
            val (_, version, flavor) = it.groupValues
            task.dependsOn(tasks.getByName("license${version}${flavor}Report"))
        }
    }
}

tasks.named("preBuild") {
    dependsOn(":ktlint", ":detekt")
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    copyJsonReportToAssets = true
}

android {
    namespace = "de.gematik.ti.erp.app"
    defaultConfig {
        applicationId = "de.gematik.ti.erp.app"
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = "de.gematik.ti.erp.app.test.test"
        testInstrumentationRunner = TEST_INSTRUMENTATION_ORCHESTRATOR
        testInstrumentationRunnerArguments += "clearPackageData" to "true"
        testInstrumentationRunnerArguments += "useTestStorageService" to "true"
    }

    androidResources {
        noCompress("srt", "csv", "json")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    dependencyCheck {
        analyzers.assemblyEnabled = false

        formats = listOf(Format.HTML, Format.XML)
        scanConfigurations = configurations.filter {
            it.name.startsWith("api") ||
                it.name.startsWith("implementation") ||
                it.name.startsWith("kapt")
        }.map {
            it.name
        }
    }

    val signingPropsFile = project.rootProject.file("signing.properties")
    if (signingPropsFile.canRead()) {
        println("Signing properties found: $signingPropsFile")
        val signingProps = Properties()
        signingProps.load(signingPropsFile.inputStream())
        signingConfigs {
            fun creatingRelease() = creating {
                val target = this.name // property name; e.g. googleRelease
                println("Create signing config for: $target")
                storeFile = signingProps["$target.storePath"]?.let { file(it) }
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingPropsFile.canRead()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept")
        }
        val debug by getting {
            applicationIdSuffix = ".test"
            resValue("string", "app_label", "eRp-Test")
            versionNameSuffix = "-debug"
            signingConfigs {
                getByName("debug") {
                    storeFile = file("$rootDir/keystore/debug.keystore")
                    keyAlias = "androiddebugkey"
                    storePassword = "android"
                    keyPassword = "android"
                }
            }
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
            }
        }
        if (flavor?.startsWith("konnektathonDevru") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".konnektathon.rudev"
                versionNameSuffix = "-konnektathon-RUDEV"
                signingConfig = signingConfigs.findByName("googleRelease")
            }
        }
    }

    packagingOptions {
        resources {
            excludes += "META-INF/**"
            // for JNA and JNA-platform
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            // for byte-buddy
            excludes += "META-INF/licenses/ASM"
            pickFirsts += "win32-x86-64/attach_hotspot_windows.dll"
            pickFirsts += "win32-x86/attach_hotspot_windows.dll"
        }
    }
}

compose.android.useAndroidX = true
compose.android.androidxVersion = app.composeVersion

dependencies {
    implementation(project(":common"))
    testImplementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    implementation("com.tom-roush:pdfbox-android:2.0.27.0") {
        exclude(group = "org.bouncycastle")
    }

    app {
        dataMatrix {
            implementation(mlkitBarcodeScanner)
            implementation(zxing)
        }
        kotlinX {
            implementation(coroutines("core"))
            implementation(coroutines("android"))
            implementation(coroutines("play-services"))
            compileOnly(datetime)
            testCompileOnly(datetime)
        }
        android {
            coreLibraryDesugaring(desugaring)

            implementation(legacySupport)
            implementation(appcompat)
            implementation(coreKtx)
            implementation(datastorePreferences)
            implementation(security)
            implementation(biometric)
            implementation(webkit)

            implementation(mapsAndroidUtils)
            implementation(maps)
            implementation(mapsCompose)
            implementation(mapsUtils)

            implementation(lifecycle("viewmodel-compose"))
            implementation(lifecycle("process")) {
                // FIXME: remove if AGP > 7.2.0-alpha05 can handle cyclic dependencies (again)
                exclude(group = "androidx.lifecycle", module = "lifecycle-runtime")
            }

            implementation(composeNavigation)
            implementation(composeActivity)
            implementation(composePaging)

            implementation(camera("camera2"))
            implementation(camera("lifecycle"))
            implementation(camera("view", cameraViewVersion))
            implementation(imageCropper)

            debugImplementation(processPhoenix)
        }
        dependencyInjection {
            compileOnly(kodein("di-framework-compose"))
            androidTestImplementation(kodein("di-framework-compose"))
        }
        logging {
            implementation(napier)
        }
        lottie {
            implementation(lottie)
        }
        serialization {
            implementation(kotlinXJson)
        }
        crypto {
            implementation(jose4j)
            implementation(bouncyCastle("bcprov"))
            implementation(bouncyCastle("bcpkix"))
            testImplementation(bouncyCastle("bcprov"))
            testImplementation(bouncyCastle("bcpkix"))
        }
        network {
            implementation(retrofit2("retrofit"))
            implementation(retrofit2KotlinXSerialization)
            implementation(okhttp3("okhttp"))
            implementation(okhttp3("logging-interceptor"))

            androidTestImplementation(okhttp3("okhttp"))
        }
        database {
            compileOnly(realm)
            testCompileOnly(realm)
        }
        compose {
            implementation(runtime)
            implementation(foundation)
            implementation(material)
            implementation(materialIconsExtended)
            implementation(animation)
            implementation(uiTooling)
            implementation(preview)
            implementation(accompanist("swiperefresh"))
            implementation(accompanist("flowlayout"))
            implementation(accompanist("pager"))
            implementation(accompanist("pager-indicators"))
            implementation(accompanist("systemuicontroller"))
        }
        passwordStrength {
            implementation(zxcvbn)
        }

        contentSquare {
            implementation(cts)
        }

        playServices {
            implementation(location)
            implementation(integrity)
            implementation(appReview)
            implementation(appUpdate)
            implementation(maps)
        }

        androidTest {
            testImplementation(archCore)
            androidTestImplementation(core)
            androidTestImplementation(rules)
            androidTestImplementation(junitExt)
            androidTestImplementation(runner)
            androidTestUtil(orchestrator)
            androidTestUtil(services)
            androidTestImplementation(navigation)
            androidTestImplementation(espresso)
            androidTestImplementation(espressoIntents)
        }
        kotlinXTest {
            testImplementation(coroutinesTest)
        }
        composeTest {
            androidTestImplementation(ui)
            androidTestImplementation(junit4)
        }
        networkTest {
            testImplementation(mockWebServer)
        }
        test {
            testImplementation(junit4)
            testImplementation(snakeyaml)
            testImplementation(json)
            testImplementation(mockk("mockk"))
            androidTestImplementation(mockk("mockk-android"))
        }
    }
}

secrets {
    defaultPropertiesFileName = if (project.rootProject.file("ci-overrides.properties").exists()) "ci-overrides.properties" else "gradle.properties"
}
