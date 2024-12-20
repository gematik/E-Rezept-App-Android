/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.debugsettings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun ClientIdsSection() {
    Column {
        ClientIdItem(
            title = "Client ID (PU)",
            value = BuildKonfig.CLIENT_ID_PU
        )
        SpacerSmall()
        ClientIdItem(
            title = "Client ID (RU)",
            value = BuildKonfig.CLIENT_ID_RU
        )
        SpacerSmall()
        ClientIdItem(
            title = "Client ID (TU)",
            value = BuildKonfig.CLIENT_ID_TU
        )
        SpacerSmall()
    }
}

@Suppress("MagicNumber")
@Composable
private fun ClientIdItem(
    title: String,
    value: String
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
            text = value
        )
    }
}

@LightDarkPreview
@Composable
fun ClientIdsSectionPreview() {
    PreviewAppTheme {
        ClientIdsSection()
    }
}
