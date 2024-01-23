/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.idp.model

import de.gematik.ti.erp.app.idp.api.models.RemoteFastTrackIdp
import de.gematik.ti.erp.app.idp.api.models.RemoteFederationIdp

data class HealthInsuranceData(
    val name: String,
    val id: String,
    val isGid: Boolean, // does it have health-insurance id enabled
    val logo: String?
) {
    companion object {

        fun HealthInsuranceData.isPkv() = id.endsWith("pkv")
        fun RemoteFederationIdp.mapToDomain() =
            HealthInsuranceData(
                name = name,
                id = id,
                isGid = isGid,
                logo = logo
            )

        fun RemoteFastTrackIdp.mapToDomain() =
            HealthInsuranceData(
                name = name,
                id = id,
                isGid = false,
                logo = null
            )
    }
}
