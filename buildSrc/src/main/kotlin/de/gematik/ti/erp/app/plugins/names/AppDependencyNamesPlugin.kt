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

package de.gematik.ti.erp.app.plugins.names

import de.gematik.ti.erp.app.ErpPlugin
import org.gradle.api.Project

class AppDependencyNamesPlugin : ErpPlugin {
    val appNameSpace = "de.gematik.ti.erp.app"
    val appId = "de.gematik.ti.erp.app"

    // module names
    val feature = ":app:features"
    val digas = ":app:digas"
    val navigation = ":app:navigation"
    val tracker = ":app:tracker"
    val fhirParser = ":fhir-parser"
    val utils = ":utils"
    val core = ":app-core"
    val uiComponents = ":ui-components"
    val demoMode = ":app:demo-mode"
    val testActions = ":app:test-actions"
    val testTags = ":app:test-tags"
    val multiplatform = ":common"

    fun moduleName(value: String) = "$appNameSpace.$value"
    fun idName(value: String) = "$appId.$value"
    override fun apply(project: Project) {
        // no-op
    }
}
