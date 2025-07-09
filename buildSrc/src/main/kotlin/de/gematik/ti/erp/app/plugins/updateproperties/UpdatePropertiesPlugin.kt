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

package de.gematik.ti.erp.app.plugins.updateproperties

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.tasks.downloadChangeLogs
import de.gematik.ti.erp.app.tasks.downloadLokaliseStrings
import de.gematik.ti.erp.app.tasks.updateApoFzdApiKeyTask
import de.gematik.ti.erp.app.tasks.updateFdApiKeysTask
import de.gematik.ti.erp.app.tasks.updateGradleProperties
import de.gematik.ti.erp.app.tasks.uploadLokaliseStrings
import org.gradle.api.Project

@Suppress("unused")
class UpdatePropertiesPlugin : ErpPlugin {
    override fun apply(project: Project) {
        project.tasks.apply {
            // User Agent and Version
            updateGradleProperties()
            // ERP Keys
            updateFdApiKeysTask(project)
            // APO FZD Keys
            updateApoFzdApiKeyTask(project)
            // Download Lokalise Strings
            downloadLokaliseStrings()
            // Upload Lokalise Strings
            uploadLokaliseStrings()
            // download Change Logs
            downloadChangeLogs()
        }
    }
}
