/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.pharmacy.api.model.SearchAccessTokenResponse
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacySearchAccessTokenRemoteDataSource
import io.realm.kotlin.types.RealmInstant
import retrofit2.Response

class DefaultPharmacySearchAccessTokenRepository(
    private val localDataSource: PharmacySearchAccessTokenLocalDataSource,
    private val remoteDataSource: PharmacySearchAccessTokenRemoteDataSource
) : PharmacySearchAccessTokenRepository {

    override val searchAccessToken = localDataSource.searchAccessToken

    override val searchAccessTokenValue = localDataSource.searchAccessTokenValue

    override suspend fun fetchNewToken(): Response<SearchAccessTokenResponse> {
        return remoteDataSource.fetchNewToken()
    }

    override suspend fun saveToken(token: String, currentTime: RealmInstant) {
        localDataSource.saveToken(token, currentTime)
    }

    override suspend fun clearToken() {
        localDataSource.clearToken()
    }
}
