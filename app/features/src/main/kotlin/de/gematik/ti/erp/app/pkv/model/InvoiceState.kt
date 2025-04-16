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

package de.gematik.ti.erp.app.pkv.model

import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class InvoiceState {
    data object LoadingOnChange : InvoiceState()
    data object NoInvoice : InvoiceState()
    class InvoiceLoaded(val record: InvoiceData.PKVInvoiceRecord) : InvoiceState()

    @Composable
    @OptIn(ExperimentalContracts::class)
    inline fun OnInvoiceLoaded(
        invoices: @Composable (InvoiceLoaded) -> Unit
    ) {
        contract {
            returns(true) implies (this@InvoiceState is InvoiceLoaded)
            returns(false) implies (this@InvoiceState is NoInvoice)
        }
        if (this is InvoiceLoaded) {
            invoices(this)
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun hasInvoice(): Boolean {
        contract {
            returns(true) implies (this@InvoiceState is InvoiceLoaded)
            returns(false) implies (this@InvoiceState is NoInvoice)
        }
        return this is InvoiceLoaded
    }
}
