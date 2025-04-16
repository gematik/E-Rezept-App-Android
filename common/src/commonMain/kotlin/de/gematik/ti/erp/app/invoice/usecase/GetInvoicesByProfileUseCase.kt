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

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetInvoicesByProfileUseCase(
    private val invoiceRepository: InvoiceRepository,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(profileId: String): Flow<Map<Year, List<InvoiceData.PKVInvoiceRecord>>> =
        invoiceRepository.invoices(profileId)
            .map { invoices ->
                invoices
                    .sortedByDescending { it.timestamp }
                    .groupBy { Year(it.timestamp.toLocalDateTime(timeZone).year) }
            }
            .flowOn(dispatcher)
}
