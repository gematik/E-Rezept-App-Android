/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.ui.retryWithAuthenticator
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.net.HttpURLConnection

interface DeletePrescriptionsBridge {
    suspend fun deletePrescription(profileId: ProfileIdentifier, taskId: String): Result<Unit>
}

@Stable
class DeletePrescriptions(
    private val bridge: DeletePrescriptionsBridge,
    private val authenticator: Authenticator
) {
    sealed interface State : PrescriptionServiceState {
        object Deleted : State

        sealed interface Error : State, PrescriptionServiceErrorState {
            object PrescriptionWorkflowBlocked : Error
            object PrescriptionNotFound : Error
        }
    }

    suspend fun deletePrescription(
        profileId: ProfileIdentifier,
        taskId: String
    ): PrescriptionServiceState =
        deletePrescriptionFlow(profileId = profileId, taskId = taskId).cancellable().first()

    private fun deletePrescriptionFlow(profileId: ProfileIdentifier, taskId: String) =
        flow {
            emit(bridge.deletePrescription(profileId = profileId, taskId = taskId))
        }.map { result ->
            result.fold(
                onSuccess = {
                    State.Deleted
                },
                onFailure = {
                    if (it is ApiCallException) {
                        when (it.response.code()) {
                            HttpURLConnection.HTTP_FORBIDDEN -> State.Error.PrescriptionWorkflowBlocked
                            HttpURLConnection.HTTP_GONE -> State.Error.PrescriptionNotFound
                            else -> throw it
                        }
                    } else {
                        throw it
                    }
                }
            )
        }
            .retryWithAuthenticator(
                isUserAction = true,
                authenticate = authenticator.authenticateForPrescriptions(profileId)
            )
            .catchAndTransformRemoteExceptions()
            .flowOn(Dispatchers.IO)
}
