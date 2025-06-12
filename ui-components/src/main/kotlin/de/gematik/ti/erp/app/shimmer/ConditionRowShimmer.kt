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

package de.gematik.ti.erp.app.shimmer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun ConditionRowShimmer(
    imageVector: ImageVector = Icons.AutoMirrored.Default.PlaylistAddCheck
) {
    Row {
        Icon(
            modifier = Modifier.weight(0.05f),
            imageVector = imageVector,
            contentDescription = null,
            tint = AppTheme.colors.neutral400
        )
        Spacer(
            modifier = Modifier.weight(0.05f)
        )
        LimitedTextShimmer(
            modifier = Modifier.weight(0.5f),
            width = SizeDefaults.twentyfourfold
        )
    }
}

@LightDarkPreview
@Composable
private fun ConditionRowShimmerPreview() {
    PreviewTheme {
        ConditionRowShimmer()
    }
}
