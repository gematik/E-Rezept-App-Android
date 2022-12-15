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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.ui.retryWithAuthenticator
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance
import java.net.HttpURLConnection
import java.util.UUID

@Stable
class RedeemPrescriptionsController(
    private val searchUseCase: PharmacySearchUseCase,
    private val overviewUseCase: PharmacyOverviewUseCase,
    private val dispatchers: DispatchProvider,
    private val authenticator: Authenticator
) {
    sealed interface State : PrescriptionServiceState {
        class Ordered(val orderId: String, val results: Map<PharmacyUseCaseData.PrescriptionOrder, Error?>) : State

        sealed interface Error : State, PrescriptionServiceErrorState {
            object Unknown : Error
            object TaskIdDoesNotExist : Error
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

    private fun orderPrescriptionsFlow(
        profileId: ProfileIdentifier,
        orderId: UUID,
        prescriptions: List<PharmacyUseCaseData.PrescriptionOrder>,
        redeemOption: PharmacyScreenData.OrderOption,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        contact: PharmacyUseCaseData.ShippingContact
    ) =
        flow {
            withContext(dispatchers.IO) {
                val results = prescriptions
                    .map { prescription ->
                        async {
                            prescription to searchUseCase.redeemPrescription(
                                orderId = orderId,
                                profileId = profileId,
                                redeemOption = when (redeemOption) {
                                    PharmacyScreenData.OrderOption.ReserveInPharmacy -> RemoteRedeemOption.Local
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
                    .toMap()

                overviewUseCase.saveOrUpdateUsedPharmacies(pharmacy)

                results.mapValues { (_, result) ->
                    result.fold(
                        onSuccess = {
                            null
                        },
                        onFailure = {
                            if (it is ApiCallException) {
                                when (it.response.code()) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> State.Error.TaskIdDoesNotExist
                                    else -> throw it
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
            .flowOn(dispatchers.IO)
}

@Composable
fun rememberRedeemPrescriptionsController(): RedeemPrescriptionsController {
    val searchUseCase by rememberInstance<PharmacySearchUseCase>()
    val overviewUseCase by rememberInstance<PharmacyOverviewUseCase>()
    val dispatchers by rememberInstance<DispatchProvider>()
    val authenticator = LocalAuthenticator.current
    return remember {
        RedeemPrescriptionsController(
            searchUseCase = searchUseCase,
            overviewUseCase = overviewUseCase,
            dispatchers = dispatchers,
            authenticator = authenticator
        )
    }
}
