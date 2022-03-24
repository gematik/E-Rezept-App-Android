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

import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacySearchResult
import javax.inject.Inject

class PharmacyRepository @Inject constructor(
    private val remoteDataSource: PharmacyRemoteDataSource
) {

    suspend fun searchPharmacies(
        names: List<String>,
        filter: Map<String, String>
    ): Result<PharmacySearchResult> =
        remoteDataSource.searchPharmacies(names, filter).map {
            PharmacyMapper.extractLocalPharmacyServices(it)
        }

    suspend fun searchPharmaciesByBundle(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<PharmacySearchResult> =
        remoteDataSource.searchPharmaciesContinued(
            bundleId = bundleId,
            offset = offset,
            count = count
        ).map { PharmacyMapper.extractLocalPharmacyServices(it) }
}
