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

package de.gematik.ti.erp.app.messages.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData

data class InvoiceMessageUiModel(
    val name: String?,
    val date: String,
    val time: String,
    val taskId: String?,
    val isFirstMessage: Boolean
) : MessageUiModel {
    companion object {
        @Composable
        fun OrderUseCaseData.OrderDetail.toInvoiceMessage(isFirstMessage: Boolean): InvoiceMessageUiModel {
            val formatter = rememberErpTimeFormatter()

            val date = remember(taskDetailedBundles.firstOrNull()?.invoiceInfo?.invoiceSentOn) {
                taskDetailedBundles.firstOrNull()?.invoiceInfo?.invoiceSentOn?.let { formatter.date(it) } ?: "n/a"
            }
            val time = remember(taskDetailedBundles.firstOrNull()?.invoiceInfo?.invoiceSentOn) {
                taskDetailedBundles.firstOrNull()?.invoiceInfo?.invoiceSentOn?.let { formatter.time(it) } ?: "n/a"
            }
            return InvoiceMessageUiModel(
                name = taskDetailedBundles.firstOrNull()?.prescription?.name,
                date = date,
                time = time,
                taskId = taskDetailedBundles.firstOrNull()?.prescription?.taskId,
                isFirstMessage = isFirstMessage
            )
        }
    }
}
