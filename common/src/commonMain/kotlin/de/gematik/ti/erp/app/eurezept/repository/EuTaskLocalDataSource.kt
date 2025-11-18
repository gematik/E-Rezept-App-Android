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

package de.gematik.ti.erp.app.eurezept.repository

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuAccessCodeEntityV1
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.toEuAccessCode
import de.gematik.ti.erp.app.eurezept.model.toEuOrderEntityV1
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EuTaskLocalDataSource(
    private val realm: Realm,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    fun getLatestEuAccessCodeByProfileIdAndCountry(profileId: ProfileIdentifier, countryCode: String): Flow<EuAccessCode?> =
        realm.query<EuAccessCodeEntityV1>("profileId = $0 AND countryCode = $1", profileId, countryCode).asFlow().map {
            it.list.maxByOrNull { it.validUntil }?.toEuAccessCode()
        }

    fun getAllEuAccessCodesForProfile(profileId: ProfileIdentifier): Flow<List<EuAccessCode>> =
        realm.query<EuAccessCodeEntityV1>("profileId = $0", profileId).asFlow().map { result ->
            result.list.sortedBy { it.createdAt }.map { it.toEuAccessCode() }
        }.flowOn(dispatchers)

    suspend fun deleteEuAccessCodeByProfileId(profileId: ProfileIdentifier) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<EuAccessCodeEntityV1>("profileId = $0", profileId)?.let { accessCode ->
                    delete(accessCode)
                }
            }
        }
    }

    suspend fun saveEuOrder(euOrder: EuOrder) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<EuAccessCodeEntityV1>("profileId = $0", euOrder.profileId)?.let { accessCode ->
                    delete(accessCode)
                }
                copyToRealm(euOrder.toEuOrderEntityV1(), UpdatePolicy.ALL)
            }
        }
    }
}
