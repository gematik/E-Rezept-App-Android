import de.gematik.ti.erp.Dependencies
import de.gematik.ti.erp.inject
import de.gematik.ti.erp.networkSecurityConfigGen.AndroidNetworkConfigGeneratorTask
import de.gematik.ti.erp.stringResGen.AndroidStringResourceGeneratorTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Locale

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("de.gematik.ti.erp.resgen")
    id("de.gematik.ti.erp.dependencies")
}

afterEvaluate {
    tasks.getByName("createDistributable").apply {
        doLast {
            // remove build env dirs
            val configFile = fileTree(outputs.files.asPath) { include("**/E-Rezept-Desktop.cfg") }.asFileTree.singleFile
            configFile.writeText(
                configFile.useLines { lines ->
                    lines
                        .filter { !it.contains(rootProject.rootDir.path, ignoreCase = true) }
                        .joinToString(System.lineSeparator())
                }
            )
        }
    }
}

tasks.withType<AndroidStringResourceGeneratorTask> {
    resourceFiles = listOf(
        stringResPath("values/strings.xml") to Locale.GERMAN,
        stringResPath("values/strings_desktop.xml") to Locale.GERMAN,
        stringResPath("values/strings_kbv_codes.xml") to Locale.GERMAN,
        stringResPath("values-en/strings.xml") to Locale.ENGLISH,
        stringResPath("values-tr/strings.xml") to Locale.forLanguageTag("tr")
    )
    outputPath = file(project.projectDir.path + "/src/jvmMain/kotlin")
    packagePath = "de.gematik.ti.erp.app.common.strings"
}

fun stringResPath(name: String): String {
    return rootDir.path + "/android/src/main/res/" + name
}

tasks.withType<AndroidNetworkConfigGeneratorTask> {
    resourceFile = file(networkConfigPath("network_security_config.xml"))
    outputPath = file(project.projectDir.path + "/src/jvmMain/kotlin")
    packagePath = "de.gematik.ti.erp.app.common.pinning"
}

fun networkConfigPath(name: String): String {
    return rootDir.path + "/android/src/main/res/xml/" + name
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = Dependencies.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))

                // TODO move to multiplatform lib for nfc
                implementation("de.gematik.ti.erp.app:smartcard-wrapper:1.0")

                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)

                implementation(compose.materialIconsExtended)

                inject {
                    androidX {
                        compileOnly(multiplatformPaging)
                    }
                    coroutines {
                        implementation(coroutinesSwing)
                    }
                    dateTime {
                        implementation(datetime)
                    }
                    dependencyInjection {
                        compileOnly(kodeinCompose)
                    }
                    dataMatrix {
                        implementation(zxing)
                    }
                    logging {
                        implementation(napier)
                        implementation(slf4jNoOp)
                    }
                    serialization {
                        implementation(fhir)
                        implementation(kotlinXJson)
                    }
                    crypto {
                        implementation(jose4j)
                        implementation(bouncycastleBcprov)
                        implementation(bouncycastleBcpkix)
                    }
                    network {
                        implementation(retrofit)
                        implementation(retrofit2KotlinXSerialization)
                        implementation(okhttp3)
                        implementation(okhttpLogging)
                        // Work around vulnerable Okio version 3.1.0 (CVE-2023-3635).
                        // Can be removed when Retrofit releases a new version >2.9.0.
                        implementation(okio)
                    }
                    database {
                        compileOnly(realm)
                    }
                }
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "de.gematik.ti.erp.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "E-Rezept-Desktop"
            packageVersion = "1.0.0"
            modules("java.smartcardio")

            macOS {
                iconFile.set(rootProject.file("resources/icon/ERezept.icns"))
            }
            windows {
                iconFile.set(
                    project.file(
                        when {
                            (project.property("buildkonfig.flavor") as? String)
                                ?.endsWith("Internal") == true -> "E-Rezept-Dev.ico"

                            else -> "E-Rezept.ico"
                        }
                    )
                )
                menuGroup = "gematik"
            }
            linux {
                iconFile.set(rootProject.file("resources/icon/ERezept.png"))
            }
        }
    }
}
