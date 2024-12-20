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

package de.gematik.ti.erp.app.invoice.model

import de.gematik.ti.erp.app.api.HttpErrorState

sealed interface InvoiceResult {
    sealed interface InvoiceSuccess : InvoiceResult {

        data object SuccessOnChargeItemBundleDownload : InvoiceSuccess

        data class SuccessOnChargeItemDownload(
            val total: Int,
            val downloaded: Int
        ) : InvoiceSuccess

        data class SuccessOnChargeItemDownloadWithErrors(
            val total: Int,
            val downloaded: Int,
            val errors: List<InvoiceError>
        ) : InvoiceSuccess

        data object SuccessOnDeletion : InvoiceSuccess
    }

    data class UserNotLoggedInError(val id: String) : InvoiceResult, Throwable()

    data class InvoiceError(val errorState: HttpErrorState) : InvoiceResult, Throwable()
    data class InvoiceCombinedError(val errorStates: List<HttpErrorState>) : InvoiceResult, Throwable()
}