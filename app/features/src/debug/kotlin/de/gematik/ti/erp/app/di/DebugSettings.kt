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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.debugsettings.data.DebugSettingsData
import de.gematik.ti.erp.app.debugsettings.data.Environment

object DebugSettings {

    fun DebugSettingsData.getDebugSettingsDataForEnvironment(
        environment: Environment? = null
    ): DebugSettingsData {
        val debugSettingsData = when (environment) {
            Environment.PU -> this.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_PU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_PU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_PU,
                pharmacyServiceActive = true
            )

            Environment.TU -> this.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_TU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_TU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )

            Environment.RU -> this.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_RU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_RU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )

            Environment.RUDEV -> this.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_RU_DEV,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_RU_DEV,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )

            Environment.TR -> this.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_TR,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_TR,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )

            null -> this.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_PU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_PU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_PU,
                pharmacyServiceActive = true
            )
        }
        return debugSettingsData
    }
}
