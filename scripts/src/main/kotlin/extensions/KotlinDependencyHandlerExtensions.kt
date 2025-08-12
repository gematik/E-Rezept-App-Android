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

package extensions

import generated.corutinestestBundle
import generated.cryptoBundle
import generated.cryptotestBundle
import generated.databaseBundle
import generated.datetimeBundle
import generated.diKotlinBundle
import generated.kotlintestBundle
import generated.kotlinxCoroutinesCoreLibrary
import generated.loggingNapierLibrary
import generated.networkBundle
import generated.networkOkhttpMockwebserverLibrary
import generated.networkRetrofitLibrary
import generated.pagingCommonKtxLibrary
import generated.serializationBundle
import generated.testjunitBundle
import org.gradle.api.artifacts.VersionCatalog
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.commonMainDependencies(versionCatalog: VersionCatalog) {
    implementation(kotlin("reflect"))
    implementation(versionCatalog.pagingCommonKtxLibrary)
    implementation(versionCatalog.kotlinxCoroutinesCoreLibrary)
    implementation(versionCatalog.datetimeBundle)
    implementation(versionCatalog.databaseBundle)
    implementation(versionCatalog.cryptoBundle)
    implementation(versionCatalog.serializationBundle)
    implementation(versionCatalog.loggingNapierLibrary)
    implementation(versionCatalog.networkBundle)
    implementation(versionCatalog.diKotlinBundle)
    implementation(RealmPaparazziFix.realmKotlinV3)
}

fun KotlinDependencyHandler.commonTestDependencies(versionCatalog: VersionCatalog) {
    implementation(versionCatalog.databaseBundle)
    implementation(versionCatalog.corutinestestBundle)
    implementation(versionCatalog.serializationBundle)
    implementation(versionCatalog.kotlintestBundle)
    implementation(versionCatalog.testjunitBundle)
    implementation(versionCatalog.cryptotestBundle)
    implementation(versionCatalog.datetimeBundle)
    implementation(versionCatalog.networkRetrofitLibrary)
    implementation(versionCatalog.networkOkhttpMockwebserverLibrary)
}
