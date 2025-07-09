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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.base.model.DownloadResourcesState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Repository for number of new prescription obtained from server on every download or refresh
 */
class DownloadResourcesStateRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val currentState = MutableStateFlow(DownloadResourcesState.NotStarted)
    private val snapshotState = MutableSharedFlow<DownloadResourcesState>(replay = 1, extraBufferCapacity = 1)

    fun updateDetailState(state: DownloadResourcesState) {
        currentState.value = state
    }

    fun updateSnapshotState(state: DownloadResourcesState) {
        scope.launch {
            snapshotState.emit(state)
        }
    }

    fun closeSnapshotState() {
        scope.launch {
            snapshotState.emit(DownloadResourcesState.Finished)
        }
    }

    fun snapshotState(): SharedFlow<DownloadResourcesState> = snapshotState.asSharedFlow()

    fun detailState(): StateFlow<DownloadResourcesState> = currentState.asStateFlow()
}
