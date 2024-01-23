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

package de.gematik.ti.erp.app.pkv.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator

import de.gematik.ti.erp.app.consent.usecase.ConsentUseCase
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.prescription.ui.GeneralErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.ui.retryWithAuthenticator

import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance
import java.net.HttpURLConnection

@Stable
class ConsentController(
    val context: Context,
    val profileId: ProfileIdentifier,
    val insuranceIdentifier: String,
    private val authenticator: Authenticator,
    private val useCase: ConsentUseCase,
    val dispatchers: DispatchProvider
) {

    sealed interface State : PrescriptionServiceState {
        object ChargeConsentNotGranted : State
        object ChargeConsentGranted : State
        object ChargeConsentRevoked : State

        sealed interface Error : State, PrescriptionServiceErrorState {
            object ChargeConsentAlreadyGranted : Error
            object ChargeConsentAlreadyRevoked : Error
        }
        fun PrescriptionServiceState.isConsentGranted() =
            this == ChargeConsentGranted || this == Error.ChargeConsentAlreadyGranted
    }

    fun getChargeConsent() = flow {
        emit(useCase.getChargeConsent(profileId))
    }.map { result ->
        result.map {
            when (it) {
                true -> State.ChargeConsentGranted
                else -> State.ChargeConsentNotGranted
            }
        }.getOrThrow()
    }.retryWithAuthenticator(
        isUserAction = true,
        authenticate = authenticator.authenticateForPrescriptions(profileId)
    )
        .catchAndTransformRemoteExceptions()
        .flowOn(Dispatchers.IO)

    fun grantChargeConsent() = flow {
        emit(useCase.grantChargeConsent(profileId, insuranceIdentifier))
    }.map { result ->
        result.fold(
            onSuccess = {
                State.ChargeConsentGranted
            },
            onFailure = {
                if (it is ApiCallException) {
                    when (it.response.code()) {
                        HttpURLConnection.HTTP_CONFLICT -> State.Error.ChargeConsentAlreadyGranted
                        else -> throw it
                    }
                } else {
                    throw it
                }
            }
        )
    }.retryWithAuthenticator(
        isUserAction = true,
        authenticate = authenticator.authenticateForPrescriptions(profileId)
    )
        .catchAndTransformRemoteExceptions()
        .flowOn(Dispatchers.IO)

    fun revokeChargeConsent() = flow {
        emit(useCase.deleteChargeConsent(profileId))
    }.map { result ->
        result.fold(
            onSuccess = {
                State.ChargeConsentRevoked
            },
            onFailure = {
                if (it is ApiCallException) {
                    when (it.response.code()) {
                        HttpURLConnection.HTTP_NOT_FOUND -> State.Error.ChargeConsentAlreadyRevoked
                        else -> throw it
                    }
                } else {
                    throw it
                }
            }
        )
    }.retryWithAuthenticator(
        isUserAction = true,
        authenticate = authenticator.authenticateForPrescriptions(profileId)
    )
        .catchAndTransformRemoteExceptions()
        .flowOn(Dispatchers.IO)
}

@Composable
fun rememberConsentController(profile: ProfilesUseCaseData.Profile): ConsentController {
    val context = LocalContext.current
    val dispatchers by rememberInstance<DispatchProvider>()
    val consentUseCase by rememberInstance<ConsentUseCase>()
    val authenticator = LocalAuthenticator.current

    return remember(profile.id, profile.insuranceInformation.insuranceIdentifier) {
        ConsentController(
            context = context,
            profileId = profile.id,
            insuranceIdentifier = profile.insuranceInformation.insuranceIdentifier,
            useCase = consentUseCase,
            authenticator = authenticator,
            dispatchers = dispatchers
        )
    }
}

fun consentErrorMessage(context: Context, consentErrorState: PrescriptionServiceErrorState): String? =
    when (consentErrorState) {
        GeneralErrorState.NetworkNotAvailable ->
            context.getString(R.string.error_message_network_not_available)
        is GeneralErrorState.ServerCommunicationFailedWhileRefreshing ->
            context.getString(R.string.error_message_server_communication_failed).format(consentErrorState.code)
        GeneralErrorState.FatalTruststoreState ->
            context.getString(R.string.error_message_vau_error)
        else -> null
    }
