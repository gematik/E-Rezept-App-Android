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

package de.gematik.ti.erp.app.database.api.eurezept

import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow

interface EuTaskLocalDataSource {
    fun observeEuOrder(orderId: String): Flow<EuOrderErpModel?>
    fun observeAllEuOrders(): Flow<List<EuOrderErpModel>>
    fun getLatestEuAccessCodeByProfileIdAndCountry(profileId: ProfileIdentifier, countryCode: String): Flow<EuAccessCodeErpModel?>
    fun getOrdersForProfileCountryAndTasks(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskIds: List<String>
    ): Flow<List<EuOrderErpModel>>
    suspend fun deleteEuAccessCodeByProfileId(profileId: ProfileIdentifier)
    suspend fun saveEuOrder(euOrder: EuOrderErpModel, eventType: EuEventType)
    suspend fun markEventsAsRead(eventIds: List<String>)
    suspend fun addEventToValidOrders(
        profileId: ProfileIdentifier,
        taskIds: List<String>,
        eventType: EuEventType
    )
    fun getEuAccessCode(accessCode: String): Flow<EuAccessCodeErpModel?>
    suspend fun addRedeemedEventIfValidOrderExists(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskId: String
    )
    suspend fun importMigratedOrder(euOrder: EuOrderErpModel) {}
}
