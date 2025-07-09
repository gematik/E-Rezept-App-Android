/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

@file:Suppress("unused")

package extensions

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

internal val VersionCatalog.androidxCoreKtxLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("androix-core-ktx")

internal val VersionCatalog.coroutinesCoreLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("kotlinx-coroutines-core")

internal val VersionCatalog.desugarLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("desugar_jdk_libs")

internal val VersionCatalog.diComposeLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("di-compose")

internal val VersionCatalog.kotlinReflectLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("kotlin-reflect")

internal val VersionCatalog.ktLintLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("quality-ktlint")

internal val VersionCatalog.detektLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("quality-detekt")

internal val VersionCatalog.multiplatformPagingLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("paging-common-ktx")

internal val VersionCatalog.napierLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("logging-napier")

internal val VersionCatalog.materialLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("material")

internal val VersionCatalog.navigationComposeLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("navigation-compose")

internal val VersionCatalog.networkOkHttpLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("network-okhttp")

internal val VersionCatalog.networkRetrofitLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("network-retrofit")

internal val VersionCatalog.networkOkhttpMockWebServerLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("network-okhttp-mockwebserver")

internal val VersionCatalog.playAppUpdateLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("play-app-update")

internal val VersionCatalog.gematikRulesLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("rules")

internal val VersionCatalog.composeBomLibrary: Provider<MinimalExternalModuleDependency>
    get() = getLibrary("androidx-compose-bom")

private fun VersionCatalog.getLibrary(library: String) = findLibrary(library).get()
