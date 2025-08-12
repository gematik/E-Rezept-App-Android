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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.api.httpErrorState
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.fhir.communication.DigaDispenseRequestBuilder
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.DigaRedeemedPrescriptionState
import de.gematik.ti.erp.app.redeem.model.MissingInformation
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Use case for redeeming a DiGA prescription.
 * It handles:
 * - Loading the local prescription
 * - Extracting IKNR
 * - Resolving telematik ID via FHIR-VZD
 * - Building the dispense request communication
 * - Sending the redemption to the insurer
 *
 * Progress can optionally be tracked using [RedeemDigaProgressState].
 */
class RedeemDigaUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val digaRepository: DigaRepository,
    private val digaDispenseRequestBuilder: DigaDispenseRequestBuilder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Optional lifecycle hooks to observe stages of the redemption process.
     */
    data class RedeemDigaProgressState(
        val onRedeemStartState: () -> Unit,
        val onTelematikIdObtained: () -> Unit,
        val onRedeemSuccess: () -> Unit,
        val onRedeemFailure: () -> Unit
    )

    /**
     * Arguments required to redeem a DiGA prescription.
     *
     * @param profileId The KVNR of the user profile
     * @param taskId Task ID of the prescription to be redeemed
     * @param orderId Logical order identifier for tracking
     * @param lifecycleHooks Optional hooks to track process steps
     */
    data class RedeemDigaArguments(
        val profileId: String,
        val taskId: String,
        val telematikId: String? = null,
        val orderId: String,
        val lifecycleHooks: RedeemDigaProgressState? = null
    )

    /**
     * Redeems a DiGA prescription using the provided [arguments].
     * Returns a [BaseRedeemState] representing the result of the redemption process.
     */
    @Suppress("CyclomaticComplexMethod")
    suspend operator fun invoke(
        arguments: RedeemDigaArguments
    ): BaseRedeemState {
        arguments.lifecycleHooks?.onRedeemStartState?.invoke()

        val prescription = withContext(dispatcher) {
            prescriptionRepository.loadSyncedTaskByTaskId(arguments.taskId).firstOrNull()
        } ?: return DigaRedeemedPrescriptionState.NotAvailableInDatabase(
            missingType = MissingInformation.TaskId,
            value = arguments.taskId
        ).also { arguments.lifecycleHooks?.onRedeemFailure?.invoke() }

        if (prescription.redeemedOn() != null) {
            return DigaRedeemedPrescriptionState.AlreadyRedeemed(orderId = arguments.orderId)
                .also { arguments.lifecycleHooks?.onRedeemFailure?.invoke() }
        }

        // get the access-code required for sending the comm-res
        val accessCode = prescription.accessCode

        // insurance identifier (kvnr) for the patient
        val kvnr = prescription.patient.insuranceIdentifier
            ?: return DigaRedeemedPrescriptionState.NotAvailableInDatabase(
                missingType = MissingInformation.Kvnr,
                value = "No Kvnr found for taskId ${arguments.taskId}"
            ).also { arguments.lifecycleHooks?.onRedeemFailure?.invoke() }

        // Extract the telematik ID from the insurance provider
        val telematikId = arguments.telematikId

        if (telematikId.isNullOrEmpty()) {
            return DigaRedeemedPrescriptionState.NotAvailableInInsuranceDirectory(
                missingTelematikId = "No Telematik ID found for taskId ${arguments.taskId}"
            ).also { arguments.lifecycleHooks?.onRedeemFailure?.invoke() }
        }

        arguments.lifecycleHooks?.onTelematikIdObtained?.invoke()

        // create the communication json element with the required parameters
        val communicationDispenseRequestJson = digaDispenseRequestBuilder
            .buildAsJson(
                orderId = arguments.orderId,
                taskId = arguments.taskId,
                telematikId = telematikId,
                kvnrNumber = kvnr,
                accessCode = accessCode
            )

        // send the communication dispense request to the insurance
        return withContext(dispatcher) {
            prescriptionRepository.redeem(
                profileId = arguments.profileId,
                communication = communicationDispenseRequestJson,
                accessCode = accessCode
            ).fold(
                onSuccess = { _ ->
                    // update the local task with the sentOn timestamp
                    digaRepository.updateDigaCommunicationSent(
                        taskId = arguments.taskId,
                        time = Clock.System.now()
                    )
                    // diga is auto-updated to in progress on sending the redeem request successfully
                    digaRepository.updateDigaStatus(
                        taskId = arguments.taskId,
                        status = DigaStatus.InProgress(Clock.System.now()),
                        lastModified = Clock.System.now()
                    )
                    arguments.lifecycleHooks?.onRedeemSuccess?.invoke()
                    Napier.d { "Successful redemption sent for diga with task-id ${arguments.taskId}" }
                    BaseRedeemState.Success
                },
                onFailure = { error ->
                    arguments.lifecycleHooks?.onRedeemFailure?.invoke()
                    when (error) {
                        is ApiCallException -> BaseRedeemState.Error(
                            errorState = error.response.httpErrorState()
                        )

                        else -> BaseRedeemState.Error(
                            errorState = HttpErrorState.ErrorWithCause(error.message ?: "Unknown error")
                        )
                    }
                }
            )
        }
    }
}
