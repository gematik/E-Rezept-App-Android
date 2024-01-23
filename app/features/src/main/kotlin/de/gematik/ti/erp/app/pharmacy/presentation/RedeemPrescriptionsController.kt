/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.fhir.model.DirectCommunicationMessage
import de.gematik.ti.erp.app.fhir.model.json
import de.gematik.ti.erp.app.orders.usecase.SaveLocalCommunicationUseCase
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyDirectRedeemUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.presentation.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.presentation.retryWithAuthenticator
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.kodein.di.compose.rememberInstance
import java.net.HttpURLConnection
import java.util.UUID

@Stable
class RedeemPrescriptionsController(
    private val searchUseCase: PharmacySearchUseCase,
    private val pharmacyDirectRedeemUseCase: PharmacyDirectRedeemUseCase,
    private val saveLocalCommunicationUseCase: SaveLocalCommunicationUseCase,
    private val overviewUseCase: PharmacyOverviewUseCase,
    private val dispatchers: DispatchProvider,
    private val authenticator: Authenticator
) {
    sealed interface State : PrescriptionServiceState {
        class Ordered(val orderId: String, val results: Map<PharmacyUseCaseData.PrescriptionOrder, State?>) :
            State

        sealed interface Success : State {
            object Ok : Success
        }

        sealed interface Error : State, PrescriptionServiceErrorState {
            object Unknown : Error
            object UnableToRedeem : Error
            object IncorrectDataStructure : Error
            object JsonViolated : Error
            object Timeout : Error
            object Conflict : Error
            object Gone : Error
            object NotFound : Error
        }
    }

    suspend fun orderPrescriptions(
        profileId: ProfileIdentifier,
        orderId: UUID,
        prescriptions: List<PharmacyUseCaseData.PrescriptionOrder>,
        redeemOption: PharmacyScreenData.OrderOption,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        contact: PharmacyUseCaseData.ShippingContact
    ): PrescriptionServiceState =
        orderPrescriptionsFlow(
            profileId = profileId,
            orderId = orderId,
            prescriptions = prescriptions,
            redeemOption = redeemOption,
            pharmacy = pharmacy,
            contact = contact
        ).cancellable().first()

    @Requirement(
        "A_22778",
        "A_22779",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Start Redeem without TI."
    )
    suspend fun orderPrescriptionsDirectly(
        orderId: UUID,
        prescriptions: List<PharmacyUseCaseData.PrescriptionOrder>,
        redeemOption: PharmacyScreenData.OrderOption,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        contact: PharmacyUseCaseData.ShippingContact
    ): PrescriptionServiceState =
        orderPrescriptionsDirectlyFlow(
            orderId = orderId,
            prescriptions = prescriptions,
            redeemOption = redeemOption,
            pharmacy = pharmacy,
            contact = contact
        ).cancellable().first()

    @Requirement(
        "GS-A_5542#2",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Errors from the protocol are handled and delegated."
    )
    private fun orderPrescriptionsDirectlyFlow(
        orderId: UUID,
        prescriptions: List<PharmacyUseCaseData.PrescriptionOrder>,
        redeemOption: PharmacyScreenData.OrderOption,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        contact: PharmacyUseCaseData.ShippingContact
    ) =
        flow {
            withContext(dispatchers.io) {
                val certHolderList = pharmacyDirectRedeemUseCase.loadCertificates(
                    pharmacy.id
                ).getOrNull()

                val transactionId = UUID.randomUUID().toString()

                val results = prescriptions
                    .map { prescription ->

                        val message = DirectCommunicationMessage(
                            version = 2,
                            supplyOptionsType = when (redeemOption) {
                                PharmacyScreenData.OrderOption.PickupService -> RemoteRedeemOption.Local.type
                                PharmacyScreenData.OrderOption.CourierDelivery -> RemoteRedeemOption.Delivery.type
                                PharmacyScreenData.OrderOption.MailDelivery -> RemoteRedeemOption.Shipment.type
                            },
                            name = contact.name,
                            address = listOf(contact.line1, contact.line2, contact.postalCode, contact.city),
                            phone = contact.telephoneNumber,
                            hint = contact.deliveryInformation,
                            text = "",
                            mail = contact.mail,
                            transactionID = transactionId,
                            taskID = prescription.taskId,
                            accessCode = prescription.accessCode
                        )
                        val messageString = json.encodeToString(message)

                        async {
                            prescription to certHolderList?.let {
                                pharmacyDirectRedeemUseCase.redeemPrescriptionDirectly(
                                    url = when (redeemOption) {
                                        PharmacyScreenData.OrderOption.CourierDelivery
                                        -> pharmacy.contacts.deliveryUrl

                                        PharmacyScreenData.OrderOption.PickupService
                                        -> pharmacy.contacts.pickUpUrl

                                        PharmacyScreenData.OrderOption.MailDelivery
                                        -> pharmacy.contacts.onlineServiceUrl
                                    },
                                    message = messageString,
                                    telematikId = pharmacy.telematikId,
                                    recipientCertificates = it,
                                    transactionId = transactionId
                                )
                            }
                        }
                    }
                    .awaitAll()
                    .toMap()

                overviewUseCase.saveOrUpdateUsedPharmacies(pharmacy)

                results.mapValues { (order, result) ->
                    result?.fold(
                        onSuccess = {
                            saveLocalCommunicationUseCase.invoke(order.taskId, pharmacy.id, transactionId)
                            State.Success.Ok
                        },
                        onFailure = {
                            if (it is ApiCallException) {
                                when (it.response.code()) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> State.Error.IncorrectDataStructure // 400
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> State.Error.JsonViolated // 401
                                    HttpURLConnection.HTTP_NOT_FOUND -> State.Error.UnableToRedeem // 404
                                    HttpURLConnection.HTTP_CLIENT_TIMEOUT -> State.Error.Timeout // 408
                                    HttpURLConnection.HTTP_CONFLICT -> State.Error.Conflict // 409
                                    HttpURLConnection.HTTP_GONE -> State.Error.Gone // 410
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> State.Error.Unknown // 500
                                    else -> {
                                        State.Error.Unknown
                                    }
                                }
                            } else {
                                State.Error.Unknown
                            }
                        }
                    )
                }
            }.also {
                emit(it)
            }
        }.map { results ->
            State.Ordered(orderId.toString(), results)
        }.flowOn(dispatchers.io)

    @Requirement(
        "GS-A_5542#3",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Errors from the protocol are handled and delegated."
    )
    private fun orderPrescriptionsFlow(
        profileId: ProfileIdentifier,
        orderId: UUID,
        prescriptions: List<PharmacyUseCaseData.PrescriptionOrder>,
        redeemOption: PharmacyScreenData.OrderOption,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        contact: PharmacyUseCaseData.ShippingContact
    ) =
        flow {
            withContext(dispatchers.io) {
                val results = prescriptions
                    .map { prescription ->
                        async {
                            prescription to searchUseCase.redeemPrescription(
                                orderId = orderId,
                                profileId = profileId,
                                redeemOption = when (redeemOption) {
                                    PharmacyScreenData.OrderOption.PickupService -> RemoteRedeemOption.Local
                                    PharmacyScreenData.OrderOption.CourierDelivery -> RemoteRedeemOption.Delivery
                                    PharmacyScreenData.OrderOption.MailDelivery -> RemoteRedeemOption.Shipment
                                },
                                order = prescription,
                                contact = contact,
                                pharmacyTelematikId = pharmacy.telematikId
                            )
                        }
                    }
                    .awaitAll()
                    .map {
                        Napier.d { "orders are $it" }
                        it
                    }
                    .toMap()

                overviewUseCase.saveOrUpdateUsedPharmacies(pharmacy)

                results.mapValues { (_, result) ->
                    result.fold(
                        onSuccess = {
                            State.Success.Ok
                        },
                        onFailure = {
                            if (it is ApiCallException) {
                                when (it.response.code()) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> State.Error.IncorrectDataStructure // 400
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> State.Error.JsonViolated // 401
                                    HttpURLConnection.HTTP_CLIENT_TIMEOUT -> State.Error.Timeout // 408
                                    HttpURLConnection.HTTP_CONFLICT -> State.Error.Conflict // 409
                                    HttpURLConnection.HTTP_GONE -> State.Error.Gone // 410
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> State.Error.Unknown // 500
                                    else -> {
                                        throw it
                                    }
                                }
                            } else {
                                throw it
                            }
                        }
                    )
                }
            }.also {
                emit(it)
            }
        }.map { results ->
            Napier.d { "State.Ordered(orderId.toString(), results) ${State.Ordered(orderId.toString(), results)}" }
            State.Ordered(orderId.toString(), results)
        }
            .retryWithAuthenticator(
                isUserAction = true,
                authenticate = authenticator.authenticateForPrescriptions(profileId)
            )
            .catchAndTransformRemoteExceptions()
            .catch {
                // TODO: remove for better error handling
                emit(State.Error.Unknown)
            }
            .flowOn(dispatchers.io)
}

@Composable
fun rememberRedeemPrescriptionsController(): RedeemPrescriptionsController {
    val searchUseCase by rememberInstance<PharmacySearchUseCase>()
    val pharmacyDirectRedeemUseCase by rememberInstance<PharmacyDirectRedeemUseCase>()
    val saveLocalCommunicationUseCase by rememberInstance<SaveLocalCommunicationUseCase>()
    val overviewUseCase by rememberInstance<PharmacyOverviewUseCase>()
    val dispatchers by rememberInstance<DispatchProvider>()
    val authenticator = LocalAuthenticator.current
    return remember {
        RedeemPrescriptionsController(
            searchUseCase = searchUseCase,
            pharmacyDirectRedeemUseCase = pharmacyDirectRedeemUseCase,
            saveLocalCommunicationUseCase = saveLocalCommunicationUseCase,
            overviewUseCase = overviewUseCase,
            dispatchers = dispatchers,
            authenticator = authenticator
        )
    }
}
