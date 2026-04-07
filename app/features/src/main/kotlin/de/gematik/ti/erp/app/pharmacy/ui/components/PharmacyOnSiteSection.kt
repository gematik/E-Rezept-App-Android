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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacyPreviewData
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
internal fun PharmacyOnSiteSection(labels: List<String>) {
    if (labels.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.search_pharmacies_section_local),
            style = AppTheme.typography.h6,
            color = AppTheme.colors.neutral900
        )
        SpacerMedium()
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
            labels.forEach { label ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppTheme.colors.green600,
                        modifier = Modifier.size(SizeDefaults.doubleHalf)
                    )
                    Text(
                        text = label,
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.neutral900
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PharmacyOnSiteSectionPreview() {
    PreviewAppTheme {
        PharmacyOnSiteSection(
            labels = PharmacyPreviewData.ALL_PRESENT_DATA.onSiteFeatures.map { it.code }
        )
    }
}
