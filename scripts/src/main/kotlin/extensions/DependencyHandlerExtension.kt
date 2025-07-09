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

import extensions.BuildNames.androidTestImplementation
import extensions.BuildNames.androidTestUtil
import extensions.BuildNames.testCompileOnly
import extensions.BuildNames.testImplementation
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.kotlin

fun DependencyHandlerScope.androidTestExtension(libs: VersionCatalog) {
    androidTestImplementation(kotlin("reflect"))
    androidTestImplementation(kotlin("stdlib"))
    androidTestImplementation(libs.diComposeLibrary)
    androidTestImplementation(libs.networkOkHttpLibrary)
    androidTestImplementation(libs.androidXTestBundle)
    androidTestImplementation(libs.composeTestBundle)
    androidTestImplementation(libs.mockkAndroidBundle)
    androidTestUtil(libs.androidXTestUtilsBundle)
}

fun DependencyHandlerScope.testExtension(libs: VersionCatalog) {
    testImplementation(kotlin("test"))
    testImplementation(libs.cryptoBundle)
    testImplementation(libs.androidxTestArchCoreBundle)
    testImplementation(libs.coroutinesTestBundle)
    testImplementation(libs.networkTestBundle)
    testImplementation(libs.composeTestBundle)
    testImplementation(libs.testingBundle)
    testCompileOnly(libs.databaseBundle)
    testCompileOnly(libs.datetimeBundle)
}

fun DependencyHandlerScope.junitExtension(libs: VersionCatalog) {
    testImplementation(libs.junitBundle)
}
