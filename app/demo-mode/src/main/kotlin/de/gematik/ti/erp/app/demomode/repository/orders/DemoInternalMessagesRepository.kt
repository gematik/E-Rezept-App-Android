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

@file:Suppress("TooManyFunctions", "MagicNumber")

package de.gematik.ti.erp.app.demomode.repository.orders

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.messages.mapper.toInternalMessage
import de.gematik.ti.erp.app.messages.model.InternalMessage
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DemoInternalMessagesRepository(
    private val demoModeDataSource: DemoModeDataSource
) : InternalMessagesRepository {
    override fun getInternalMessages(): Flow<List<InternalMessage>> =
        demoModeDataSource.internalMessages.map {
            it.map {
                    internalMessageEntityV1 ->
                internalMessageEntityV1.toInternalMessage()
            }
        }

    override fun getUnreadInternalMessagesCount(): Flow<Long> =
        demoModeDataSource.unreadInternalMessagesCount

    override fun getLastUpdatedVersion(): Flow<String?> =
        demoModeDataSource.lastUpdatedVersion

    override suspend fun setInternalMessagesAsRead() {
        // No-op
    }

    override suspend fun updateInternalMessage(internalMessage: InternalMessage) {
        // No-op
    }

    override suspend fun saveInternalMessage(internalMessage: InternalMessage) {
        // no-op
    }
}
