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

import org.gradle.api.tasks.util.PatternFilterable

internal object BuildNames {
    const val targetDesktop = "desktop"
    const val minifiedDebug = "minifiedDebug"
    const val versionCatalogLibrary = "libs"
    const val androidTestImplementation = "androidTestImplementation"
    const val androidTestUtil = "androidTestUtil"
    const val testImplementation = "testImplementation"
    const val testCompileOnly = "testCompileOnly"
    const val implementation = "implementation"

    // virtual devices
    val pixel5Api30 = VirtualDevice("pixel5api30", "Pixel 5", 30, "aosp-atd")
    val pixel8Api34 = VirtualDevice("pixel8api34", "Pixel 8", 34, "aosp-atd")

    const val jacocoToolsVersion = "0.8.11"
}

internal object RealmPaparazziFix {
    const val realmKotlinV3 = "io.realm.kotlin:library-base:3.0.0"
}

internal data class VirtualDevice(
    val name: String,
    val device: String,
    val apiLevel: Int,
    val systemImageSource: String
)

fun PatternFilterable.excludeList() = apply {
    exclude("**/build/**")
    exclude("build/**")
    exclude("**/generated/**")
    exclude("**/android/print/**")
    exclude("**/de/gematik/ti/erp/app/**/ui/**")
    exclude("**/de/gematik/ti/erp/**/navigation/*")
    exclude("**/de/gematik/ti/erp/**/model/*")
    exclude("**/de/gematik/ti/erp/**/components/*")
    exclude("**/de/gematik/ti/erp/app/utils/*")
    exclude("**/de/gematik/ti/erp/app/di/*")
    exclude("**/de/gematik/ti/erp/app/**/di/*")
    exclude("**/de/gematik/ti/erp/app/theme/*")
    exclude("**/R.class")
    exclude("**/R$*.class")
    exclude("**/BuildConfig.*")
    exclude("**/Manifest*.*")
    exclude("android/**/*.*")
    exclude("**/de/gematik/ti/erp/app/TestTags.class")
    exclude("**/de/gematik/ti/erp/app/testactions/**")
}
