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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence").
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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.messages.mapper.toInAppMessage
import de.gematik.ti.erp.app.messages.model.InAppMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetCombinedMessagesAsInAppMessageUseCase(
    private val getExternalInAppMessagesUseCase: GetExternalInAppMessagesUseCase,
    private val getInternalMessagesUseCase: GetInternalMessagesUseCase,
    private val getLatestEuOrderMessageAsInAppMessageUseCase: GetLatestEuOrderMessageAsInAppMessageUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(currentAppLanguage: String): Flow<List<InAppMessage>> =
        combine(
            getExternalInAppMessagesUseCase(),
            getInternalMessagesUseCase.invoke(currentAppLanguage),
            getLatestEuOrderMessageAsInAppMessageUseCase.invoke()
        ) { externalMessages, internalMessages, euMessages ->
            val summarizedInternalMessage = internalMessages.firstOrNull()?.copy(
                isUnread = internalMessages.any { it.isUnread }
            )

            buildList {
                addAll(externalMessages)
                addAll(listOfNotNull(summarizedInternalMessage).map { it.toInAppMessage() })
                addAll(euMessages)
            }.sortedByDescending { it.timeState.timestamp }
        }.flowOn(dispatcher)
}
