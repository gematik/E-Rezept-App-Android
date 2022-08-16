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

package de.gematik.ti.erp.app.communication.ui

import de.gematik.ti.erp.app.communication.ui.model.CommunicationScreenData
import de.gematik.ti.erp.app.communication.usecase.CommunicationUseCase
import de.gematik.ti.erp.app.core.DispatchersProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class CommunicationViewModel(
    private val dispatchersProvider: DispatchersProvider,
    private val communicationUseCase: CommunicationUseCase
) {
    val defaultState =
        CommunicationScreenData.State(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun screenState(): Flow<CommunicationScreenData.State> =
        communicationUseCase.pharmacyCommunications().map {
            CommunicationScreenData.State(it)
        }.flowOn(dispatchersProvider.unconfined())
}
