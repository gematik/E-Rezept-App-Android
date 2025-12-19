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
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mappers.getDispenseMessageTitle
import de.gematik.ti.erp.app.messages.mappers.getInfoText
import de.gematik.ti.erp.app.messages.mappers.getSentOnTime

data class DispenseMessageUiModel(
    val text: AnnotatedString,
    val sentOn: String,
    val infoChipText: String,
    val isTheOnlyMessage: Boolean
) : MessageUiModel {
    companion object {
        @Composable
        fun OrderUseCaseData.OrderDetail.toDispenseMessage(
            isTheOnlyMessage: Boolean
        ): DispenseMessageUiModel {
            return DispenseMessageUiModel(
                text = getDispenseMessageTitle(),
                sentOn = getSentOnTime(),
                infoChipText = getInfoText(),
                isTheOnlyMessage = isTheOnlyMessage
            )
        }
    }
}
