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

package de.gematik.ti.erp.app.pkv.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HTTP_BAD_REQUEST
import de.gematik.ti.erp.app.api.HTTP_CONFLICT
import de.gematik.ti.erp.app.api.HTTP_FORBIDDEN
import de.gematik.ti.erp.app.api.HTTP_INTERNAL_ERROR
import de.gematik.ti.erp.app.api.HTTP_METHOD_NOT_ALLOWED
import de.gematik.ti.erp.app.api.HTTP_NOT_FOUND
import de.gematik.ti.erp.app.api.HTTP_REQUEST_TIMEOUT
import de.gematik.ti.erp.app.api.HTTP_TOO_MANY_REQUESTS
import de.gematik.ti.erp.app.api.HTTP_UNAUTHORIZED
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.net.UnknownHostException

@Stable
class ConsentController(
    val context: Context,
    val profileId: ProfileIdentifier,
    val insuranceIdentifier: String,
    private val getConsentUseCase: GetConsentUseCase,
    private val grantConsentUseCase: GrantConsentUseCase,
    private val revokeConsentUseCase: RevokeConsentUseCase,
    private val scope: CoroutineScope
) {

    private val consentMutableState = MutableStateFlow<PrescriptionServiceState>(ConsentState.UnknownConsent)

    private val consentErrorMutableState = MutableStateFlow<PrescriptionServiceState>(ConsentErrorState.NoError)

    val consentState: StateFlow<PrescriptionServiceState> = consentMutableState

    val consentErrorState: StateFlow<PrescriptionServiceState> = consentErrorMutableState

    sealed interface ConsentState : PrescriptionServiceState {

        // before getting the consent
        data object UnknownConsent : ConsentState
        data object NotGranted : ConsentState
        data object Granted : ConsentState
        data object Revoked : ConsentState
    }

    sealed interface ConsentErrorState : PrescriptionServiceErrorState {

        data object NoError : ConsentErrorState
        data object AlreadyGranted : ConsentErrorState
        data object ChargeConsentAlreadyRevoked : ConsentErrorState
        data object InternalError : ConsentErrorState
        data object ServerTimeout : ConsentErrorState
        data object Unauthorized : ConsentErrorState
        data object TooManyRequests : ConsentState
        data object NoInternet : ConsentErrorState
        data object BadRequest : ConsentErrorState
        data object Forbidden : ConsentErrorState
        data object Unknown : ConsentErrorState
    }

    private val isConsentGranted by lazy {
        consentMutableState.value == ConsentState.Granted ||
            consentErrorState.value == ConsentErrorState.AlreadyGranted
    }

    val isConsentGrantedState
        @Composable
        get() = isConsentGranted

    fun getChargeConsent() {
        scope.launch {
            getConsentUseCase(profileId)
                .fold(
                    onSuccess = { result ->
                        when (result) {
                            true -> ConsentState.Granted
                            else -> ConsentState.NotGranted
                        }
                        setNoError()
                    },
                    onFailure = {
                        mapConsentErrorStates(it)
                    }
                )
        }
    }

    fun grantChargeConsent() {
        scope.launch {
            grantConsentUseCase(profileId, insuranceIdentifier)
                .fold(
                    onSuccess = {
                        consentMutableState.value = ConsentState.Granted
                        setNoError()
                    },
                    onFailure = {
                        mapConsentErrorStates(it)
                    }
                )
        }
    }

    private fun setNoError() {
        consentErrorMutableState.value = ConsentErrorState.NoError
    }

    fun revokeChargeConsent() {
        scope.launch {
            revokeConsentUseCase(profileId)
                .fold(
                    onSuccess = {
                        consentMutableState.value = ConsentState.Revoked
                    },
                    onFailure = {
                        mapConsentErrorStates(it)
                    }
                )
        }
    }

    private fun mapConsentErrorStates(error: Throwable) {
        if (error.cause?.cause is UnknownHostException) {
            consentErrorMutableState.value = ConsentErrorState.NoInternet
        } else {
            val errorCode = (error as? ApiCallException)?.response?.code()
            consentErrorMutableState.value = when (errorCode) {
                HTTP_CONFLICT -> ConsentErrorState.AlreadyGranted
                HTTP_REQUEST_TIMEOUT -> ConsentErrorState.ServerTimeout
                HTTP_INTERNAL_ERROR -> ConsentErrorState.InternalError
                HTTP_TOO_MANY_REQUESTS -> ConsentErrorState.TooManyRequests
                HTTP_NOT_FOUND -> ConsentErrorState.ChargeConsentAlreadyRevoked
                HTTP_BAD_REQUEST, HTTP_METHOD_NOT_ALLOWED -> ConsentErrorState.BadRequest
                HTTP_FORBIDDEN -> ConsentErrorState.Forbidden
                HTTP_UNAUTHORIZED -> ConsentErrorState.Unauthorized
                else -> {
                    ConsentErrorState.Unknown // silent fail
                }
            }
        }
    }
}

@Composable
fun rememberConsentController(profile: ProfilesUseCaseData.Profile): ConsentController {
    val context = LocalContext.current

    val getConsentUseCase by rememberInstance<GetConsentUseCase>()
    val grantConsentUseCase by rememberInstance<GrantConsentUseCase>()
    val revokeConsentUseCase by rememberInstance<RevokeConsentUseCase>()

    val scope = rememberCoroutineScope()

    return remember(profile.id, profile.insurance.insuranceIdentifier) {
        ConsentController(
            context = context,
            profileId = profile.id,
            insuranceIdentifier = profile.insurance.insuranceIdentifier,
            getConsentUseCase = getConsentUseCase,
            grantConsentUseCase = grantConsentUseCase,
            revokeConsentUseCase = revokeConsentUseCase,
            scope = scope
        )
    }
}
