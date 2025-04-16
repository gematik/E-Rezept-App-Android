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

package de.gematik.ti.erp.app.api

import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.UnknownHostException

//region sources
// https://github.com/gematik/api-erp/blob/master/docs/erp_chargeItem.adoc#anwendungsfall-abrechnungsinformation-zum-%C3%A4ndern-abrufen
// https://github.com/gematik/api-erp/blob/master/docs/erp_communication.adoc
// https://github.com/gematik/api-erp/blob/master/docs/erp_consent.adoc
//endregion

@Requirement(
    "O.Source_3#1",
    "O.Source_4#1",
    "O.Plat_4#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Error messages are localized using the `HttpErrorState " +
        "Search for `HttpErrorState` to see all instances." +
        "Most errors are localized with static text. Logging is only active on debug builds."
)
@Requirement(
    "A_19937#2",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Mapping of errors"
)
@Suppress("MagicNumber")
sealed class HttpErrorState(val errorCode: Int) {
    data object Unknown : HttpErrorState(-1)
    data object BadRequest : HttpErrorState(HttpURLConnection.HTTP_BAD_REQUEST)
    data object Unauthorized : HttpErrorState(HttpURLConnection.HTTP_UNAUTHORIZED)
    data object Forbidden : HttpErrorState(HttpURLConnection.HTTP_FORBIDDEN)
    data object NotFound : HttpErrorState(HttpURLConnection.HTTP_NOT_FOUND)
    data object MethodNotAllowed : HttpErrorState(HttpURLConnection.HTTP_BAD_METHOD)
    data object RequestTimeout : HttpErrorState(HttpURLConnection.HTTP_CLIENT_TIMEOUT)
    data object Conflict : HttpErrorState(HttpURLConnection.HTTP_CONFLICT) // not present in docs
    data object Gone : HttpErrorState(HttpURLConnection.HTTP_GONE)
    data object TooManyRequest : HttpErrorState(429)
    data object ServerError : HttpErrorState(HttpURLConnection.HTTP_INTERNAL_ERROR)
    data class ErrorWithCause(val message: String) : HttpErrorState(-1)
}

// TODO: Use code from OperationOutcome
/*
  {
    "resourceType":"OperationOutcome",
    "meta":{
        "profile":["http://hl7.org/fhir/StructureDefinition/OperationOutcome"]
        },
        "issue":[
            {
                "severity":"error",
                "code":"invalid",
                "details":{"text":"Referenced task does not contain a KVNR"}
            }
        ]
    }
 */
@Suppress("MagicNumber")
fun Response<*>.httpErrorState(): HttpErrorState {
    Napier.e { "http error code ${code()}" }
    try {
        return when (this.code()) {
            HttpURLConnection.HTTP_BAD_REQUEST -> HttpErrorState.BadRequest // 400
            HttpURLConnection.HTTP_UNAUTHORIZED -> HttpErrorState.Unauthorized // 401
            HttpURLConnection.HTTP_FORBIDDEN -> HttpErrorState.Forbidden // 403
            HttpURLConnection.HTTP_NOT_FOUND -> HttpErrorState.NotFound // 404
            HttpURLConnection.HTTP_BAD_METHOD -> HttpErrorState.MethodNotAllowed // 405
            HttpURLConnection.HTTP_CLIENT_TIMEOUT -> HttpErrorState.RequestTimeout // 408
            HttpURLConnection.HTTP_CONFLICT -> HttpErrorState.Conflict // 409
            HttpURLConnection.HTTP_GONE -> HttpErrorState.Gone // 410
            429 -> HttpErrorState.TooManyRequest // 429
            HttpURLConnection.HTTP_INTERNAL_ERROR -> HttpErrorState.ServerError // 500
            else -> {
                HttpErrorState.ErrorWithCause("Unknown error ${this.code()}")
            }
        }
    } catch (error: Throwable) {
        if (error.cause?.cause is UnknownHostException) {
            Napier.e { "Error on no internet" }
        } else {
            Napier.e("Unknown error on http call", error)
        }
        return HttpErrorState.Unknown
    }
}
