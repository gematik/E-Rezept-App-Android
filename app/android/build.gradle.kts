
import de.gematik.ti.erp.AppDependenciesPlugin
import de.gematik.ti.erp.Dependencies
import de.gematik.ti.erp.inject
import de.gematik.ti.erp.overriding
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
    id("io.realm.kotlin")
    kotlin("plugin.serialization")
    id("org.owasp.dependencycheck")
    id("com.jaredsburrows.license")
    id("de.gematik.ti.erp.dependencies")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin")
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
    namespace = AppDependenciesPlugin.APP_NAME_SPACE
    defaultConfig {
        applicationId = AppDependenciesPlugin.APP_ID
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = "de.gematik.ti.erp.app.test.test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kotlinOptions {
        jvmTarget = Dependencies.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    dependencyCheck {
        analyzers.assemblyEnabled = false
        suppressionFile = "${project.rootDir}" + "/config/dependency-check/suppressions.xml"
        formats = listOf(Format.HTML, Format.XML)
        scanConfigurations = configurations.filter {
            it.name.startsWith("api") ||
                it.name.startsWith("implementation") ||
                it.name.startsWith("kapt")
        }.map {
            it.name
        }
    }
    val rootPath = project.rootProject
    val signingPropsFile = rootPath.file("signing.properties")
    if (signingPropsFile.canRead()) {
        println("Signing properties found: $signingPropsFile")
        val signingProps = Properties()
        signingProps.load(signingPropsFile.inputStream())
        signingConfigs {
            fun creatingRelease() = creating {
                val target = this.name // property name; e.g. googleRelease
                println("Create signing config for: $target")
                storeFile = signingProps["$target.storePath"]?.let {
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
                "proguard-rules.pro"
            )
            if (signingPropsFile.canRead()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept")
        }
        val debug by getting {
            applicationIdSuffix = ".test"
            resValue("string", "app_label", "E-Rezept Debug")
            versionNameSuffix = "-debug"
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

dependencies {
    implementation(project(":app:features"))
    implementation(project(":app:demo-mode"))
    androidTestImplementation(project(":app:shared-test"))
    implementation(project(":common"))
    testImplementation(project(":common"))
    testImplementation(kotlin("test"))
    implementation("com.tom-roush:pdfbox-android:2.0.27.0") {
        exclude(group = "org.bouncycastle")
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
    }

    inject {
        dateTime {
            implementation(datetime)
            testCompileOnly(datetime)
        }
        android {
            coreLibraryDesugaring(desugaring)
            debugImplementation(processPhoenix)
        }
        androidX {
            implementation(appcompat)
            implementation(composeNavigation)
            implementation(security)
            implementation(lifecycleViewmodel)
            implementation(lifecycleProcess)
            implementation(lifecycleComposeRuntime)
        }
        dependencyInjection {
            compileOnly(kodeinCompose)
            androidTestImplementation(kodeinCompose)
        }
        logging {
            implementation(napier)
        }
        tracking {
            implementation(contentSquare)
        }
        compose {
            implementation(runtime)
            implementation(foundation)
            implementation(uiTooling)
            implementation(preview)
        }
        crypto {
            testImplementation(jose4j)
            testImplementation(bouncycastleBcprov)
            testImplementation(bouncycastleBcpkix)
        }
        database {
            testCompileOnly(realm)
        }
        network {
            implementation(retrofit)
            implementation(retrofit2KotlinXSerialization)
            implementation(okhttp3)
            implementation(okhttpLogging)
            // Work around vulnerable Okio version 3.1.0 (CVE-2023-3635).
            // Can be removed as soon as Retrofit releases a new version >2.9.0.
            implementation(okio)

            androidTestImplementation(okhttp3)
        }
        database {
            compileOnly(realm)
            testCompileOnly(realm)
        }
        playServices {
            implementation(appUpdate)
        }
        serialization {
            implementation(kotlinXJson)
        }
        androidXTest {
            testImplementation(archCore)
            androidTestImplementation(core)
            androidTestImplementation(rules)
            androidTestImplementation(junitExt)
            androidTestImplementation(runner)
            androidTestUtil(orchestrator)
            androidTestUtil(services)
            // androidTestImplementation(navigation)
            androidTestImplementation(espresso)
            androidTestImplementation(espressoIntents)
        }
        tracing {
            debugImplementation(tracing)
            implementation(tracing)
        }
        coroutinesTest {
            testImplementation(coroutinesTest)
        }
        composeTest {
            androidTestImplementation(ui)
            debugImplementation(uiManifest)
            androidTestImplementation(junit4)
        }
        networkTest {
            testImplementation(mockWebServer)
        }
        test {
            testImplementation(junit4)
            testImplementation(snakeyaml)
            testImplementation(json)
            testImplementation(mockkOld)
            androidTestImplementation(mockkAndroid)
        }
    }
}

secrets {
    defaultPropertiesFileName = if (project.rootProject.file("ci-overrides.properties").exists()
    ) "ci-overrides.properties" else "gradle.properties"
}
