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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.local

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.SearchAccessTokenEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.safeWrite
import de.gematik.ti.erp.app.db.updateOrCreate
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Requirement(
    "O.Auth_13#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Search access-token is stored securely in the local data source."
)
class PharmacySearchAccessTokenLocalDataSource(
    private val realm: Realm
) {
    val searchAccessToken: Flow<SearchAccessTokenEntityV1?> = realm.query<SearchAccessTokenEntityV1>().asFlow().map { it.list.firstOrNull() }

    val searchAccessTokenValue: Flow<String?> =
        realm.query<SearchAccessTokenEntityV1>().asFlow().map { it.list.firstOrNull()?.accessToken }

    suspend fun saveToken(token: String, currentTime: RealmInstant = RealmInstant.now()) {
        realm.updateOrCreate(
            queryBlock = { query<SearchAccessTokenEntityV1>().first().find() }
        ) {
            it.accessToken = token
            it.lastUpdate = currentTime
        }
    }

    suspend fun clearToken() {
        realm.safeWrite {
            queryFirst<SearchAccessTokenEntityV1>()?.apply {
                accessToken = ""
                lastUpdate = RealmInstant.MIN
            }
        }
    }
}
