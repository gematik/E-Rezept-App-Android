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
package de.gematik.ti.erp.app.database.realm.v1.pharmacy

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.updateOrCreate
import de.gematik.ti.erp.app.pharmacy.model.SearchAccessTokenErpModel
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

@Requirement(
    "O.Auth_13#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Search access-token is stored securely in the local data source."
)
internal class PharmacySearchAccessTokenLocalDataSourceV1(
    private val realm: Realm
) : PharmacySearchAccessTokenLocalDataSource {

    override val searchAccessToken: Flow<SearchAccessTokenErpModel?> =
        realm.query<SearchAccessTokenEntityV1>().asFlow().map {
            it.list.firstOrNull()?.toErpModel()
        }

    override suspend fun saveToken(token: String, currentTime: Instant) {
        realm.updateOrCreate(
            queryBlock = { query<SearchAccessTokenEntityV1>().first().find() }
        ) {
            it.accessToken = token
            it.lastUpdate = currentTime.toRealmInstant()
        }
    }

    override suspend fun clearToken() {
        realm.safeWrite {
            queryFirst<SearchAccessTokenEntityV1>()?.apply {
                accessToken = ""
                lastUpdate = RealmInstant.MIN
            }
        }
    }

    private fun SearchAccessTokenEntityV1.toErpModel(): SearchAccessTokenErpModel =
        SearchAccessTokenErpModel(
            accessToken = this.accessToken,
            lastUpdate = this.lastUpdate.toInstant()
        )
}
