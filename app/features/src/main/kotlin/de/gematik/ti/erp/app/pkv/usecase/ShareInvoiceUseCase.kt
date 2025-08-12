/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.pkv.usecase

import android.content.Context
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.PkvHtmlTemplate
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.pkv.FileProviderAuthority
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareInvoiceUseCase(
    private val repository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        invoice: InvoiceData.PKVInvoiceRecord,
        context: Context,
        fileProviderAuthority: FileProviderAuthority
    ) {
        withContext(dispatcher) {
            val html = PkvHtmlTemplate.createHTML(invoice)
            val file = createSharableFileInCache(context, "invoices", "invoice")
            writePdfFromHtml(context, "Invoice_${invoice.taskId}", html, file)
            repository.loadInvoiceAttachments(invoice.taskId)?.let { attachments ->
                writePDFAttachments(file, attachments)
            }
            val subject = invoice.medicationRequest.medication?.name() + "_" +
                invoice.timestamp.asFhirTemporal().formattedString()
            sharePDFFile(context, file, subject, fileProviderAuthority)
        }
    }
}
