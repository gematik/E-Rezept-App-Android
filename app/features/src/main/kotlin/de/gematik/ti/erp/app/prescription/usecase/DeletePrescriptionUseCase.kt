/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.ErpServiceState
import de.gematik.ti.erp.app.api.HTTP_BAD_REQUEST
import de.gematik.ti.erp.app.api.HTTP_FORBIDDEN
import de.gematik.ti.erp.app.api.HTTP_INTERNAL_ERROR
import de.gematik.ti.erp.app.api.HTTP_METHOD_NOT_ALLOWED
import de.gematik.ti.erp.app.api.HTTP_TOO_MANY_REQUESTS
import de.gematik.ti.erp.app.api.HTTP_UNAUTHORIZED
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.prescription.presentation.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.net.HttpURLConnection
import java.net.UnknownHostException

@Requirement(
    "A_19229-01#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User can delete a locally and remotely stored prescription and all its linked resources."
)
class DeletePrescriptionUseCase(
    private val repository: PrescriptionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    sealed interface DeletePrescriptionState : ErpServiceState {
        sealed interface ValidState : DeletePrescriptionState {
            data object Deleted : ValidState

            data object NotDeleted : ValidState
        }

        sealed interface ErrorState : DeletePrescriptionState {
            data object BadRequest : ErrorState

            data object Unauthorized : ErrorState

            data object NoInternet : ErrorState

            data object ErpWorkflowBlocked : ErrorState

            data object Unknown : ErrorState

            data object MethodNotAllowed : ErrorState

            data object TooManyRequests : ErrorState

            data object InternalError : ErrorState
        }
    }

    suspend operator fun invoke(
        profileId: ProfileIdentifier,
        taskId: String,
        deleteLocallyOnly: Boolean
    ): Flow<ErpServiceState> =
        flowOf(
            if (!repository.wasProfileEverAuthenticated(profileId) || deleteLocallyOnly) {
                // delete local saved tasks, if sso token is null
                // (profile was never connected and has imported/scanned task)
                repository.deleteLocalTaskById(taskId)
                repository.deleteLocalInvoicesById(taskId)
                DeletePrescriptionState.ValidState.Deleted
            } else {
                repository
                    .deleteRemoteTaskById(profileId = profileId, taskId = taskId)
                    .fold(
                        onSuccess = {
                            repository.deleteLocalTaskById(taskId)
                            repository.deleteLocalInvoicesById(taskId)
                            DeletePrescriptionState.ValidState.Deleted
                        },
                        onFailure = {
                            when (it) {
                                is ApiCallException -> {
                                    when (it.response.code()) {
                                        HttpURLConnection.HTTP_GONE,
                                        HttpURLConnection.HTTP_NOT_FOUND
                                        -> {
                                            repository.deleteLocalTaskById(taskId)
                                            DeletePrescriptionState.ValidState.Deleted
                                        }

                                        else -> mapDeleteErrorStates(it)
                                    }
                                }

                                is RefreshFlowException -> {
                                    DeletePrescriptionState.ErrorState.Unauthorized
                                }

                                else -> {
                                    mapDeleteErrorStates(it)
                                }
                            }
                        }
                    )
            }
        ).catchAndTransformRemoteExceptions().flowOn(dispatcher)

    /**
     * Map the error states to the corresponding [DeletePrescriptionState.ErrorState]
     * Follows the specification of
     * <a href="https://github.com/gematik/api-erp/blob/master/docs/erp_versicherte.adoc#ein-e-rezept-löschen">
     * gemSpec_eRp_FdV</a>
     */
    private fun mapDeleteErrorStates(error: Throwable): DeletePrescriptionState.ErrorState {
        if (error.cause?.cause is UnknownHostException) {
            return DeletePrescriptionState.ErrorState.NoInternet
        } else {
            return when ((error as? ApiCallException)?.response?.code()) {
                HTTP_BAD_REQUEST -> DeletePrescriptionState.ErrorState.BadRequest // 400
                HTTP_UNAUTHORIZED -> DeletePrescriptionState.ErrorState.Unauthorized // 401
                HTTP_FORBIDDEN -> DeletePrescriptionState.ErrorState.ErpWorkflowBlocked // 403
                HTTP_METHOD_NOT_ALLOWED -> DeletePrescriptionState.ErrorState.MethodNotAllowed // 405
                HTTP_TOO_MANY_REQUESTS -> DeletePrescriptionState.ErrorState.TooManyRequests // 429
                HTTP_INTERNAL_ERROR -> DeletePrescriptionState.ErrorState.InternalError // 500

                else -> {
                    // silent fail
                    DeletePrescriptionState.ErrorState.Unknown
                }
            }
        }
    }
}
