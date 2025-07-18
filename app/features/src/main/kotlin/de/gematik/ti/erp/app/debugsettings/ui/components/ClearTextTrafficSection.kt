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

import android.security.NetworkSecurityPolicy
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.capitalizeFirstChar

@Suppress("MagicNumber")
@Composable
fun ClearTextTrafficSection() {
    Row {
        Text(
            modifier = Modifier.weight(0.7f),
            style = AppTheme.typography.subtitle2,
            text = "Clear text traffic allowed"
        )
        Text(
            modifier = Modifier.weight(0.3f),
            style = AppTheme.typography.subtitle2,
            text = NetworkSecurityPolicy
                .getInstance()
                .isCleartextTrafficPermitted
                .toString()
                .capitalizeFirstChar()
        )
    }
}

@LightDarkPreview
@Composable
fun ClearTextTrafficSectionPreview() {
    PreviewAppTheme {
        ClearTextTrafficSection()
    }
}
