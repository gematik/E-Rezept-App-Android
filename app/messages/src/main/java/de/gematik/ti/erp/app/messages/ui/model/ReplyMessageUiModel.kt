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
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mappers.ReplyMessageType
import de.gematik.ti.erp.app.messages.mappers.getReplyMessageDescription
import de.gematik.ti.erp.app.messages.mappers.getReplyMessageTitle
import de.gematik.ti.erp.app.messages.mappers.getSentOnTime
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription

data class ReplyMessageUiModel(
    val title: String?,
    val description: Pair<ReplyMessageType, String>,
    val sentOn: String,
    val showInfoChip: Boolean,
    val isEnabled: Boolean,
    val isFirstMessage: Boolean,
    val isLastMessage: Boolean,
    val prescriptionsLinked: List<Prescription?>
) : MessageUiModel {
    companion object {
        @Composable
        fun OrderUseCaseData.Message.toReplyMessage(
            isFirstMessage: Boolean,
            isLastMessage: Boolean
        ): ReplyMessageUiModel {
            return ReplyMessageUiModel(
                title = getReplyMessageTitle(),
                description = getReplyMessageDescription(),
                sentOn = getSentOnTime(),
                showInfoChip = isTaskIdCountMatching && prescriptions.size != 1,
                isEnabled = type != OrderUseCaseData.Message.Type.Text,
                isFirstMessage = isFirstMessage,
                isLastMessage = isLastMessage,
                prescriptionsLinked = prescriptions
            )
        }
    }
}
