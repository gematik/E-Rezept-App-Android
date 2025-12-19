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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.messages.ui.model.DispenseMessageUiModel
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
internal fun DispenseMessage(
    item: DispenseMessageUiModel,
    onClickPharmacy: () -> Unit
) {
    MessageTimeline(
        drawFilledTop = !item.isTheOnlyMessage,
        drawFilledBottom = false, // Dispense is always the last message
        isClickable = true,
        onClick = onClickPharmacy,
        timestamp = {
            Text(
                text = item.sentOn,
                style = AppTheme.typography.subtitle2
            )
        },
        content = {
            InfoChip(item.infoChipText)

            Text(
                text = item.text,
                style = AppTheme.typography.body2
            )
        }
    )
}

@LightDarkPreview
@Composable
private fun DispenseMessagePreview() {
    PreviewTheme {
        val fakeItem = DispenseMessageUiModel(
            text = AnnotatedString("Ihr Rezept wurde erfolgreich beliefert."),
            sentOn = "12 Mär 2025 • 09:42",
            infoChipText = "Beliefert",
            isTheOnlyMessage = false
        )

        Column(Modifier.padding(SizeDefaults.double)) {
            DispenseMessage(
                item = fakeItem,
                onClickPharmacy = {}
            )
        }
    }
}
