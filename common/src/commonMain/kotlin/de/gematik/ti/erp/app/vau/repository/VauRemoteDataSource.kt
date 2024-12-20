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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList

class VauRemoteDataSource(
    private val service: VauService
) {
    suspend fun loadCertificates() =
        safeApiCall("Failed to GET vau certificates") { service.getCertList() }

    suspend fun loadOcspResponses(): Result<UntrustedOCSPList> =
        safeApiCall("Failed to GET ocsp responses") { service.getOcspResponses() }
}
