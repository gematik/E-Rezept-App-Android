/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("Unused")

package de.gematik.ti.erp

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import de.gematik.ti.erp.Dependencies.Versions.JavaVersion
import de.gematik.ti.erp.Dependencies.Versions.SdkVersions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

@Suppress("UnstableApiUsage")
class AppDependenciesPlugin : Plugin<Project> {
    val overrideProperties = Properties()

    override fun apply(project: Project) {
        // set android & compose options for all android plugins: `android { ... }`
        project.plugins.all {
            when (this) {
                is AppPlugin -> {
                    project.extensions.getByType(BaseAppModuleExtension::class).apply {
                        applyBaseProperties()
                        project.forceLibraryVersions()
                        androidResources {
                            noCompress.addAll(listOf("srt", "csv", "json"))
                        }
                        compileSdk = SdkVersions.COMPILE_SDK_VERSION
                        // done only for app-module and not for lib modules since it throws error
                        compileOptions {
                            isCoreLibraryDesugaringEnabled = true
                        }
                        defaultConfig {
                            testInstrumentationRunnerArguments += "clearPackageData" to "true"
                            testInstrumentationRunnerArguments += "useTestStorageService" to "true"
                        }
                    }
                }
            }
            when (this) {
                is LibraryPlugin -> {
                    project.extensions.getByType(LibraryExtension::class).apply {
                        applyBaseProperties()
                        project.forceLibraryVersions()
                        compileSdk = SdkVersions.COMPILE_SDK_VERSION
                    }
                }
            }
        }

        // Values in local.properties will override values with same names
        val propertiesFile = project.rootProject.file("ci-overrides.properties")
        if (propertiesFile.exists()) {
            overrideProperties.load(propertiesFile.inputStream())
        }
    }

    companion object {

        const val APP_NAME_SPACE = "de.gematik.ti.erp.app"
        const val APP_ID = "de.gematik.ti.erp.app"

        private fun Project.forceLibraryVersions() {
            configurations.all {
                resolutionStrategy {
                    // Forcing this lib to be of this version for all modules since later version needs CompileSdk = 34
                    force("androidx.emoji2:emoji2:1.3.0")
                }
            }
        }

        // these are common to app and library modules
        private fun BaseExtension.applyBaseProperties() {
            buildToolsVersion = "33.0.1"
            composeOptions.kotlinCompilerExtensionVersion = "1.5.3"
            defaultConfig {
                minSdk = SdkVersions.MIN_SDK_VERSION
                targetSdk = SdkVersions.TARGET_SDK_VERSION
            }
            compileOptions {
                sourceCompatibility = JavaVersion.PROJECT_JAVA_VERSION
                targetCompatibility = JavaVersion.PROJECT_JAVA_VERSION
            }
            testOptions {
                execution = "ANDROIDX_TEST_ORCHESTRATOR"
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
    }
}
