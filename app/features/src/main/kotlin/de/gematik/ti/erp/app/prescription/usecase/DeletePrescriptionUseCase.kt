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
import de.gematik.ti.erp.app.invoice.mapper.mapUnitToInvoiceError
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
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
    private val prescriptionRepository: PrescriptionRepository,
    private val invoiceRepository: InvoiceRepository,
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
            if (!prescriptionRepository.wasProfileEverAuthenticated(profileId) || deleteLocallyOnly) {
                // delete local saved tasks, if sso token is null
                // (profile was never connected and has imported/scanned task)
                prescriptionRepository.deleteLocalTaskById(taskId)
                invoiceRepository.deleteLocalInvoiceById(taskId)
                DeletePrescriptionState.ValidState.Deleted
            } else {
                prescriptionRepository
                    .deleteRemoteTaskById(profileId = profileId, taskId = taskId)
                    .fold(
                        onSuccess = {
                            prescriptionRepository.deleteLocalTaskById(taskId)
                            invoiceRepository.deleteRemoteInvoiceById(taskId = taskId, profileId = profileId)
                                .mapUnitToInvoiceError { invoiceRepository.deleteLocalInvoiceById(taskId) }
                            DeletePrescriptionState.ValidState.Deleted
                        },
                        onFailure = {
                            when (it) {
                                is ApiCallException -> {
                                    when (it.response.code()) {
                                        HttpURLConnection.HTTP_GONE,
                                        HttpURLConnection.HTTP_NOT_FOUND
                                        -> {
                                            prescriptionRepository.deleteLocalTaskById(taskId)
                                            invoiceRepository.deleteLocalInvoiceById(taskId)
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
        ).flowOn(dispatcher)

    /**
     * Map the error states to the corresponding [DeletePrescriptionState.ErrorState]
     * Follows the specification of
     * <a href="https://github.com/gematik/api-erp/blob/master/docs/erp_versicherte.adoc#ein-e-rezept-löschen">
     * gemSpec_eRp_FdV</a>
     */

    private fun mapDeleteErrorStates(error: Throwable): DeletePrescriptionState.ErrorState {
        return if (error.cause?.cause is UnknownHostException) {
            DeletePrescriptionState.ErrorState.NoInternet
        } else {
            when ((error as? ApiCallException)?.response?.code()) {
                HTTP_BAD_REQUEST -> DeletePrescriptionState.ErrorState.BadRequest
                HTTP_UNAUTHORIZED -> DeletePrescriptionState.ErrorState.Unauthorized
                HTTP_FORBIDDEN -> DeletePrescriptionState.ErrorState.ErpWorkflowBlocked
                HTTP_METHOD_NOT_ALLOWED -> DeletePrescriptionState.ErrorState.MethodNotAllowed
                HTTP_TOO_MANY_REQUESTS -> DeletePrescriptionState.ErrorState.TooManyRequests
                HTTP_INTERNAL_ERROR -> DeletePrescriptionState.ErrorState.InternalError
                else -> DeletePrescriptionState.ErrorState.Unknown
            }
        }
    }
}
