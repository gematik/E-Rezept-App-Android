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

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import kotlinx.coroutines.flow.Flow

class DefaultInternalMessagesRepository(
    private val internalMessagesLocalDataSource: InternalMessagesLocalDataSource
) : InternalMessagesRepository {
    override fun getInternalMessages(): Flow<List<InternalMessageErpModel>> =
        internalMessagesLocalDataSource.getInternalMessages()

    override fun getUnreadInternalMessagesCount(): Flow<Long> =
        internalMessagesLocalDataSource.getUnreadInternalMessagesCount()

    override fun getLastUpdatedVersion(): Flow<String?> =
        internalMessagesLocalDataSource.getLastUpdatedVersion()

    override suspend fun setInternalMessagesAsRead() =
        internalMessagesLocalDataSource.setInternalMessagesAsRead()

    override suspend fun updateInternalMessage(internalMessage: InternalMessageErpModel) =
        internalMessagesLocalDataSource.updateInternalMessage(updatedModel = internalMessage)

    override suspend fun saveInternalMessage(internalMessage: InternalMessageErpModel) =
        internalMessagesLocalDataSource.saveInternalMessage(internalMessageErpModel = internalMessage)
}
