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

import generated.androidxTestArchCoreBundle
import generated.androidxtestBundle
import generated.androidxtestutilsBundle
import generated.composetestBundle
import generated.corutinestestBundle
import generated.cryptoBundle
import generated.databaseBundle
import generated.datetimeBundle
import generated.diComposeLibrary
import generated.mockandroidBundle
import generated.networkOkhttpLibrary
import generated.networktestBundle
import generated.testingBundle
import generated.testjunitBundle
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.kotlin

private fun DependencyHandlerScope.androidTestImplementation(dependency: Any) {
    implementationInScope("androidTestImplementation", dependency)
}

private fun DependencyHandlerScope.androidTestUtil(dependency: Any) {
    implementationInScope("androidTestUtil", dependency)
}

private fun DependencyHandlerScope.testImplementation(dependency: Any) {
    implementationInScope("testImplementation", dependency)
}

private fun DependencyHandlerScope.testCompileOnly(dependency: Any) {
    implementationInScope("testImplementation", dependency)
}

private fun DependencyHandlerScope.implementationInScope(configurationName: String, dependency: Any) {
    add(configurationName, dependency)
}

fun DependencyHandlerScope.androidTestExtension(libs: VersionCatalog) {
    androidTestImplementation(kotlin("reflect"))
    androidTestImplementation(kotlin("stdlib"))
    androidTestImplementation(libs.diComposeLibrary)
    androidTestImplementation(libs.networkOkhttpLibrary)
    androidTestImplementation(libs.androidxtestBundle)
    androidTestImplementation(libs.composetestBundle)
    androidTestImplementation(libs.mockandroidBundle)
    androidTestUtil(libs.androidxtestutilsBundle)
}

fun DependencyHandlerScope.testExtension(libs: VersionCatalog) {
    testImplementation(kotlin("test"))
    testImplementation(libs.cryptoBundle)
    testImplementation(libs.androidxTestArchCoreBundle)
    testImplementation(libs.corutinestestBundle)
    testImplementation(libs.networktestBundle)
    testImplementation(libs.composetestBundle)
    testImplementation(libs.testingBundle)
    testCompileOnly(libs.databaseBundle)
    testCompileOnly(libs.datetimeBundle)
}

fun DependencyHandlerScope.junitExtension(libs: VersionCatalog) {
    testImplementation(libs.testjunitBundle)
}
