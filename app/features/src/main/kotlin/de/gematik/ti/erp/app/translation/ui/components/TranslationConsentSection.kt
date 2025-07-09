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

package de.gematik.ti.erp.app.translation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny

@Composable
internal fun TranslationConsentSection(
    isConsentGiven: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SizeDefaults.double, vertical = SizeDefaults.oneHalf)
    ) {
        Text(
            text = when {
                isConsentGiven -> "Offline text translations are enabled. You can manage your consent at any time."
                else -> "To enable offline text translations, we require your consent to download and store language models on your device."
            },
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
            style = MaterialTheme.typography.body2
        )

        SpacerTiny()

        Text(
            text = "You can change your consent anytime in the settings below.",
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.caption
        )

        SpacerSmall()

        TextButton(
            onClick = onClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Translation Consent")
        }
    }
}
