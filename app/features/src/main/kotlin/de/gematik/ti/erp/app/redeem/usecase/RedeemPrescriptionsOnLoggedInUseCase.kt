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
import de.gematik.ti.erp.app.fhir.communication.CommunicationDispenseRequest.createCommunicationDispenseRequest
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationPayload
import de.gematik.ti.erp.app.pharmacy.mapper.toRedeemOption
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

private val json = Json {
    encodeDefaults = true
    prettyPrint = false
}

/**
 * 1. Inform the UI on the process start
 * 2. Create the communication message in the FHIR Json format
 * 3. Send the encrypted message to the pharmacy with the prescription information in an async manner
 * 4. Collect the results on the responses for all the prescriptions
 * 5. Inform the UI on the process end
 * 6. Combine the results together in [RedeemedPrescriptionState.OrderCompleted] and return the flow
 */
class RedeemPrescriptionsOnLoggedInUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        profileId: ProfileIdentifier,
        redeemOption: PharmacyScreenData.OrderOption,
        orderId: UUID,
        prescriptionOrderInfos: List<PharmacyUseCaseData.PrescriptionInOrder>,
        contact: PharmacyUseCaseData.ShippingContact,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        onRedeemProcessStart: () -> Unit = {},
        onRedeemProcessEnd: () -> Unit = {}
    ): Flow<RedeemedPrescriptionState.OrderCompleted> =
        flow {
            withContext(dispatcher) {
                onRedeemProcessStart()
                prescriptionOrderInfos
                    .map { prescriptionOrderInfo ->
                        async {
                            val (flowTypeCode, flowTypeDisplay) = FhirCommunicationConstants.determineFlowType(prescriptionOrderInfo.taskId)

                            val communicationDispenseRequestJson = createCommunicationDispenseRequest(
                                orderId = orderId.toString(),
                                taskId = prescriptionOrderInfo.taskId,
                                accessCode = prescriptionOrderInfo.accessCode,
                                recipientId = pharmacy.telematikId,
                                payloadContent = CommunicationPayload(
                                    supplyOptionsType = redeemOption.toRedeemOption().type,
                                    name = contact.name,
                                    address = listOf(contact.line1, contact.line2, contact.postalCode, contact.city),
                                    phone = contact.telephoneNumber,
                                    hint = contact.deliveryInformation
                                ),
                                flowTypeCode = flowTypeCode,
                                flowTypeDisplay = flowTypeDisplay
                            )

                            // save the pharmacy as often used when the prescription was redeemed successfully
                            launch { pharmacyRepository.markPharmacyAsOftenUsed(pharmacy) }

                            // redeem the prescription
                            prescriptionOrderInfo to prescriptionRepository.redeem(
                                profileId = profileId,
                                communication = communicationDispenseRequestJson,
                                accessCode = prescriptionOrderInfo.accessCode
                            )
                        }
                    }
                    .awaitAll()
                    .toMap()
                    .mapValues { (prescriptionOrderInfo, redeemResult) ->
                        redeemResult.fold(
                            onSuccess = { jsonElement ->
                                Napier.i { "Prescription redeemed successfully (${prescriptionOrderInfo.title} ): $jsonElement" }
                                onRedeemProcessEnd()
                                BaseRedeemState.Success
                            },
                            onFailure = { error ->
                                Napier.e { "Error on prescription redemption (${prescriptionOrderInfo.title}) ${error.stackTraceToString()}" }
                                onRedeemProcessEnd()
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
            }.also { emit(it) }
        }
            .map { RedeemedPrescriptionState.OrderCompleted(orderId = orderId.toString(), results = it) }
            .cancellable()
}
