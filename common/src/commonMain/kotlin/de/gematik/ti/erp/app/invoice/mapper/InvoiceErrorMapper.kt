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

package de.gematik.ti.erp.app.invoice.mapper

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.api.httpErrorState
import de.gematik.ti.erp.app.invoice.model.InvoiceResult
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceError
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

inline fun Result<Unit>.mapUnitToInvoiceError(
    transform: (Unit) -> InvoiceResult
): Result<InvoiceResult> {
    return try {
        when {
            isSuccess -> {
                runCatching { transform(this.getOrThrow()) }
            }

            else -> {
                when (val exception = this.exceptionOrNull()) {
                    is ApiCallException -> Result.failure(InvoiceError(exception.response.httpErrorState()))
                    else -> Result.failure(InvoiceError(HttpErrorState.Unknown))
                }
            }
        }
    } catch (e: Exception) {
        Napier.e { "ErrorOnDeletion: ${e.message}" }
        Result.failure(InvoiceError(HttpErrorState.ErrorWithCause(e.message ?: "Unknown error on invoice charge bundle download")))
    }
}

inline fun Result<JsonElement>.mapJsonToInvoiceError(
    transform: (value: JsonElement) -> InvoiceResult
): Result<InvoiceResult> {
    return try {
        when {
            isSuccess -> {
                runCatching { transform(this.getOrThrow()) }
            }

            else -> {
                when (val exception = this.exceptionOrNull()) {
                    is ApiCallException -> Result.failure(InvoiceError(exception.response.httpErrorState()))
                    else -> Result.failure(InvoiceError(HttpErrorState.Unknown))
                }
            }
        }
    } catch (e: Exception) {
        Napier.e { "ErrorOnChargeItemDownload: result is success but data not available ${e.message}" }
        Result.failure(InvoiceError(HttpErrorState.ErrorWithCause(e.message ?: "Unknown error on invoice charge item download")))
    }
}
