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

package de.gematik.ti.erp.app.debugsettings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun ClientIdsSection(active: String) {
    Column {
        ClientIdItem(
            title = "Client ID (active)",
            value = active,
            active = active
        )
        SpacerMedium()
        ClientIdItem(
            title = "Client ID (PU)",
            value = BuildKonfig.CLIENT_ID_PU,
            active = active
        )
        SpacerSmall()
        ClientIdItem(
            title = "Client ID (RU)",
            value = BuildKonfig.CLIENT_ID_RU,
            active = active
        )
        SpacerSmall()
        ClientIdItem(
            title = "Client ID (TU)",
            value = BuildKonfig.CLIENT_ID_TU,
            active = active
        )
        SpacerSmall()
    }
}

@Suppress("MagicNumber")
@Composable
private fun ClientIdItem(
    title: String,
    value: String,
    active: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(SizeDefaults.triple)) {
        Text(
            modifier = Modifier.weight(0.4f),
            style = AppTheme.typography.subtitle2,
            text = title
        )
        Text(
            modifier = Modifier.weight(0.6f),
            style = AppTheme.typography.subtitle2,
            text = value,
            fontWeight = if (active == value) FontWeight.ExtraBold else FontWeight.Normal,
            textDecoration = if (active == value) TextDecoration.Underline else TextDecoration.None
        )
    }
}

@LightDarkPreview
@Composable
fun ClientIdsSectionPreview() {
    PreviewAppTheme {
        ClientIdsSection(BuildKonfig.CLIENT_ID_TU)
    }
}
