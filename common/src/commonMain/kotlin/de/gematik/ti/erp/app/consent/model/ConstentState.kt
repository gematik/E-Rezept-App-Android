/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.consent.model

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.ErpServiceState
import de.gematik.ti.erp.app.api.HTTP_BAD_REQUEST
import de.gematik.ti.erp.app.api.HTTP_CONFLICT
import de.gematik.ti.erp.app.api.HTTP_FORBIDDEN
import de.gematik.ti.erp.app.api.HTTP_INTERNAL_ERROR
import de.gematik.ti.erp.app.api.HTTP_METHOD_NOT_ALLOWED
import de.gematik.ti.erp.app.api.HTTP_NOT_FOUND
import de.gematik.ti.erp.app.api.HTTP_REQUEST_TIMEOUT
import de.gematik.ti.erp.app.api.HTTP_TOO_MANY_REQUESTS
import de.gematik.ti.erp.app.api.HTTP_UNAUTHORIZED
import io.github.aakira.napier.Napier
import okio.IOException

enum class ConsentContext {
    GetConsent,
    GrantConsent,
    RevokeConsent
}

@Stable
sealed interface ConsentState : ErpServiceState {

    @Stable
    sealed interface ValidState : ConsentState {
        data object UnknownConsent : ConsentState
        data object Loading : ConsentState
        data object NotGranted : ConsentState
        data class Granted(val context: ConsentContext) : ConsentState
        data object Revoked : ConsentState
    }

    @Stable
    sealed interface ConsentErrorState : ConsentState {
        data object AlreadyGranted : ConsentErrorState
        data object ChargeConsentAlreadyRevoked : ConsentErrorState
        data class InternalError(val context: ConsentContext) : ConsentErrorState
        data class ServerTimeout(val context: ConsentContext) : ConsentErrorState
        data object Unauthorized : ConsentErrorState
        data class TooManyRequests(val context: ConsentContext) : ConsentErrorState
        data class NoInternet(val context: ConsentContext) : ConsentErrorState
        data object BadRequest : ConsentErrorState
        data object Forbidden : ConsentErrorState
        data object Unknown : ConsentErrorState
    }

    companion object {
        fun ConsentState.isNotGranted(): Boolean =
            (this != ValidState.Granted(ConsentContext.GetConsent) && this != ValidState.Granted(ConsentContext.GrantConsent)) &&
                (this == ValidState.NotGranted || this == ValidState.Revoked)

        fun ConsentState.isConsentGranted(): Boolean =
            this == ValidState.Granted(ConsentContext.GetConsent) ||
                this == ValidState.Granted(ConsentContext.GrantConsent) ||
                this == ConsentErrorState.AlreadyGranted
    }
}

// TODO: Use http error states
fun mapConsentErrorStates(error: Throwable, context: ConsentContext): ErpServiceState {
    Napier.e { "consent error code ${error.cause?.cause}" }
    return when (error) {
        is ApiCallException -> {
            val errorCode = (error as? ApiCallException)?.response?.code()
            when (errorCode) {
                HTTP_CONFLICT -> ConsentState.ConsentErrorState.AlreadyGranted
                HTTP_REQUEST_TIMEOUT -> ConsentState.ConsentErrorState.ServerTimeout(context)
                HTTP_INTERNAL_ERROR -> ConsentState.ConsentErrorState.InternalError(context)
                HTTP_TOO_MANY_REQUESTS -> ConsentState.ConsentErrorState.TooManyRequests(context)
                HTTP_NOT_FOUND -> ConsentState.ConsentErrorState.ChargeConsentAlreadyRevoked
                HTTP_BAD_REQUEST, HTTP_METHOD_NOT_ALLOWED -> ConsentState.ConsentErrorState.BadRequest
                HTTP_FORBIDDEN -> ConsentState.ConsentErrorState.Forbidden
                HTTP_UNAUTHORIZED -> ConsentState.ConsentErrorState.Unauthorized
                else -> ConsentState.ConsentErrorState.Unknown // silent fail
            }
        }

        is IOException -> ConsentState.ConsentErrorState.Unknown // TODO: We use this because the safeApiCall is wrongly configured.
        else -> ConsentState.ConsentErrorState.Unknown
    }
}
