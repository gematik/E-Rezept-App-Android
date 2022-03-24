/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.api.PharmacySearchService
import de.gematik.ti.erp.app.api.safeApiCall
import org.hl7.fhir.r4.model.Bundle
import javax.inject.Inject

class PharmacyRemoteDataSource @Inject constructor(
    private val service: PharmacySearchService,
) {

    suspend fun searchPharmacies(
        names: List<String>,
        filter: Map<String, String>
    ): Result<Bundle> = safeApiCall("error searching pharmacies") {
        service.search(names, filter)
    }

    suspend fun searchPharmaciesContinued(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<Bundle> = safeApiCall("error searching pharmacies") {
        service.searchByBundle(bundleId = bundleId, offset = offset, count = count)
    }
}
