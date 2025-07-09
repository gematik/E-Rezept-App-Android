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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.debugsettings.data.DebugSettingsData
import de.gematik.ti.erp.app.debugsettings.data.Environment

object DebugSettings {

    @Suppress("UnusedParameter")
    fun DebugSettingsData.getDebugSettingsDataForEnvironment(
        environment: Environment? = null
    ): DebugSettingsData {
        val item = this.copy(
            eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI,
            eRezeptActive = true,
            idpUrl = BuildKonfig.IDP_SERVICE_URI,
            idpActive = true,
            apoVzdPharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI,
            fhirVzdPharmacyServiceUrl = BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI,
            pharmacyServiceActive = true
        )
        return item
    }
}
