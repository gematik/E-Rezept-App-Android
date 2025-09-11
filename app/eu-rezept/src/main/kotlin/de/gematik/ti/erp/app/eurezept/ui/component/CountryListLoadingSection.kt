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

package de.gematik.ti.erp.app.eurezept.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXLarge

@Composable
fun CountryListLoadingSection() {
    Column(
        modifier = Modifier
            .shimmer()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PaddingDefaults.ShortMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Right
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(SizeDefaults.triple),
                color = AppTheme.colors.primary700,
                strokeWidth = SizeDefaults.quarter
            )
            SpacerTiny()
            Text(
                text = stringResource(R.string.eu_country_selection_country_current_location),
                style = AppTheme.typography.body1,
                color = AppTheme.colors.primary700,
                fontWeight = FontWeight.Medium
            )
        }
        SpacerMedium()
        repeat(6) {
            CountryRowLabelShimmer(painter = painterResource(id = R.drawable.eu_flag_stars))
            SpacerXLarge()
        }
    }
}

@LightDarkPreview
@Composable
private fun OverviewLoadingSectionPreview() {
    PreviewTheme {
        CountryListLoadingSection()
    }
}
