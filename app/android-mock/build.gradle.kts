import de.gematik.ti.erp.Dependencies
import de.gematik.ti.erp.inject
import de.gematik.ti.erp.overriding
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

// TODO: Duplicate of android build.gradle, make this into one
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

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    copyJsonReportToAssets = true
}

android {
    namespace = "de.gematik.ti.erp.app.mock"
    defaultConfig {
        applicationId = "de.gematik.ti.erp.app.mock"
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = "de.gematik.ti.erp.app"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        noCompress("srt", "csv", "json")
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

    buildTypes {
        val release by getting {
            resValue("string", "app_label", "E-Rezept Mock")
        }
        val debug by getting {
            applicationIdSuffix = ".debug"
            resValue("string", "app_label", "E-Rezept Mock")
            versionNameSuffix = "-debug"
        }
        create("minifiedDebug") {
            initWith(debug)
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
            compileOnly(realm)
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
            testImplementation(mockk)
            androidTestImplementation(mockkAndroid)
        }
    }
}

secrets {
    defaultPropertiesFileName = if (project.rootProject.file("ci-overrides.properties").exists()
    ) "ci-overrides.properties" else "gradle.properties"
}
