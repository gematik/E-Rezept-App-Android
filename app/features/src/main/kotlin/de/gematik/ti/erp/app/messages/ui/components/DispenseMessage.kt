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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.messages.ui.model.DispenseMessageUiModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@Composable
internal fun DispenseMessage(
    item: DispenseMessageUiModel,
    onClickPharmacy: () -> Unit
) {
    // State for dynamically tracking the circle position
    val circleYPositionState = remember { mutableFloatStateOf(0f) }

    Row(
        Modifier
            .drawConnectedLine(
                drawFilledTop = !item.isTheOnlyMessage,
                drawFilledBottom = false, // dispense is always the last message in the list
                circleYPosition = { circleYPositionState.floatValue }
            )
            .clickable(
                onClick = onClickPharmacy
            )
    ) {
        Spacer(Modifier.width(SizeDefaults.triple))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            SpacerMedium()
            Row(
                modifier = Modifier.fillMaxWidth()
                    .calculateVerticalCenter(
                        onCenterCalculated = { circleYPositionState.floatValue = it }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.sentOn,
                    style = AppTheme.typography.subtitle2
                )
            }
            InfoChip(item.infoChipText)
            Text(
                text = item.text,
                style = AppTheme.typography.body2
            )
        }
    }
}
