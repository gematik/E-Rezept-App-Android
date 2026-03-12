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

package de.gematik.ti.erp.app.settings.repository

import de.gematik.ti.erp.app.database.settings.ConsentVersion
import de.gematik.ti.erp.app.database.settings.ConsentVersionDataStore
import de.gematik.ti.erp.app.fhir.constant.consent.ConsentConstants
import kotlinx.coroutines.flow.first

interface ConsentVersionRepository {
    suspend fun getConsentVersion(): ConsentConstants.ErpCharge
}

class DefaultConsentVersionRepository(
    private val dataStore: ConsentVersionDataStore?,
    private val isDebugMode: Boolean
) : ConsentVersionRepository {
    override suspend fun getConsentVersion(): ConsentConstants.ErpCharge {
        return if (isDebugMode && dataStore != null) {
            when (dataStore.consentVersion.first()) {
                ConsentVersion.V1_0 -> ConsentConstants.ErpCharge.V1_0
                ConsentVersion.V1_1 -> ConsentConstants.ErpCharge.V1_1
            }
        } else {
            ConsentConstants.ErpCharge.DEFAULT
        }
    }
}
