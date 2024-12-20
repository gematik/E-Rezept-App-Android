/*
 * Copyright 2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

@file:Suppress("unused")

package extensions

import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

// Bundles
internal val VersionCatalog.accompanistBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("accompanist")

internal val VersionCatalog.androidxAppBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("androidx-app")

internal val VersionCatalog.androidxBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("androidx")

internal val VersionCatalog.androidxTestArchCoreBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("androidx-test-arch-core")

internal val VersionCatalog.animationBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("animation")

internal val VersionCatalog.composeAppBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("compose-app")

internal val VersionCatalog.composeBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("compose")

internal val VersionCatalog.composeTestBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("composetest")

internal val VersionCatalog.cameraBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("camera")

internal val VersionCatalog.coroutinesBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("coroutines")

internal val VersionCatalog.coroutinesTestBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("corutinestest")

internal val VersionCatalog.cryptoBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("crypto")

internal val VersionCatalog.databaseBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("database")

internal val VersionCatalog.datetimeBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("datetime")

internal val VersionCatalog.datamatrixBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("datamatrix")

internal val VersionCatalog.diBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("di")

internal val VersionCatalog.diKotlinBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("di-kotlin")

internal val VersionCatalog.diViewModelBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("di-viewmodel")

internal val VersionCatalog.imageBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("image")

internal val VersionCatalog.junitBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("testjunit")

internal val VersionCatalog.kotlinBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("kotlin")

internal val VersionCatalog.kotlinTestBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("kotlintest")

internal val VersionCatalog.lifecycleBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("lifecycle")

internal val VersionCatalog.loggingBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("logging")

internal val VersionCatalog.mapsBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("maps")

internal val VersionCatalog.networkBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("network")

internal val VersionCatalog.networkTestBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("networktest")

internal val VersionCatalog.othersBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("others")

internal val VersionCatalog.pdfboxBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("pdfbox")

internal val VersionCatalog.playBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("play")

internal val VersionCatalog.processPhoenixBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("processphoenix")

internal val VersionCatalog.serializationBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("serialization")

internal val VersionCatalog.trackingBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("tracking")

internal val VersionCatalog.testingBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("testing")

internal val VersionCatalog.androidXTestBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("androidxtest")

internal val VersionCatalog.androidXTestUtilsBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("androidxtestutils")

internal val VersionCatalog.mockkAndroidBundle: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("mockandroid")

internal val VersionCatalog.detektComposeRules: Provider<ExternalModuleDependencyBundle>
    get() = getBundle("qualitydetektcomposerules")

private fun VersionCatalog.getBundle(bundle: String) = findBundle(bundle).get()
