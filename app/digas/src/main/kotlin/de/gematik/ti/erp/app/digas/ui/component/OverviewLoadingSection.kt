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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.shimmer.ConditionRowShimmer
import de.gematik.ti.erp.app.shimmer.LimitedTextShimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.SpacerXXLarge

@Composable
internal fun OverviewLoadingSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
            .shimmer()
    ) {
        LimitedTextShimmer(width = SizeDefaults.twentyfold)
        SpacerMedium()
        LimitedTextShimmer(width = SizeDefaults.twentyfourfold)
        SpacerMedium()
        LimitedTextShimmer(width = SizeDefaults.twentyfourfold)
        SpacerMedium()
        LimitedTextShimmer(width = SizeDefaults.sixteenfold)
        SpacerXXLarge()
        ConditionRowShimmer()
        SpacerXLarge()
        ConditionRowShimmer()
        SpacerXLarge()
        ConditionRowShimmer()
        SpacerXLarge()
        ConditionRowShimmer()
        SpacerXLarge()
        Divider(color = AppTheme.colors.neutral400, thickness = SizeDefaults.eighth)
        SpacerXLarge()
        ConditionRowShimmer(
            imageVector = Icons.Outlined.Lightbulb
        )
        SpacerXLarge()
        ConditionRowShimmer(
            imageVector = Icons.Outlined.CalendarToday
        )
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewLoadingSectionPreview() {
    PreviewTheme {
        OverviewLoadingSection()
    }
}
