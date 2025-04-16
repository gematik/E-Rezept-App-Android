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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
internal fun PharmacySearchErrorHint(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    action: String? = null,
    onClickAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            if (action != null && onClickAction != null) {
                TextButton(onClick = onClickAction) {
                    Text(action)
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun PharmacySearchErrorHintPreview() {
    PreviewAppTheme {
        PharmacySearchErrorHint(
            title = "Item has some error",
            subtitle = "Subtitle saying that the item has some error",
            action = "Retry",
            onClickAction = {}
        )
    }
}
