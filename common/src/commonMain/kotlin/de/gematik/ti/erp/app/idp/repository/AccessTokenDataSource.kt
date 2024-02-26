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

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant

data class AccessToken(
    val accessToken: String,
    val expiresOn: Instant
)

class AccessTokenDataSource {

    @Requirement(
        "A_21328#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Store access token in data structure only."
    )
    private val decryptedAccessTokenMap: MutableStateFlow<Map<String, AccessToken>> =
        MutableStateFlow(mutableMapOf())

    fun delete(profileId: ProfileIdentifier) {
        decryptedAccessTokenMap.update {
            it - profileId
        }
    }

    fun get(profileId: ProfileIdentifier): Flow<AccessToken?> =
        decryptedAccessTokenMap.map { it[profileId] }.distinctUntilChanged()

    fun save(profileId: ProfileIdentifier, accessToken: AccessToken) {
        decryptedAccessTokenMap.update {
            it + (profileId to accessToken)
        }
    }
}
