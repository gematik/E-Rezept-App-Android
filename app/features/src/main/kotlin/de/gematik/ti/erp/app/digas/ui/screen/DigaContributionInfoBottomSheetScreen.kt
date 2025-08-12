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

package de.gematik.ti.erp.app.digas.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class DigaContributionInfoBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = false, withCloseButton = true) {
    @Composable
    override fun Content() {
        DigaContributionInfoBottomSheetScreenContent()
    }
}

@Composable
private fun DigaContributionInfoBottomSheetScreenContent() {
    Column(
        Modifier
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge),
        horizontalAlignment = androidx.compose.ui.Alignment.Start
    ) {
        SpacerMedium()
        Text(
            stringResource(R.string.diga_contribution_info_info_header),
            style = AppTheme.typography.subtitle1,
            color = AppTheme.colors.neutral900
        )
        SpacerSmall()
        Text(
            stringResource(R.string.diga_contribution_info_info_text),
            style = AppTheme.typography.body2l
        )
        SpacerMedium()

        Text(
            stringResource(R.string.diga_contribution_info_info_date),
            style = AppTheme.typography.body2l.copy(fontStyle = FontStyle.Italic)
        )
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
internal fun DigaContributionInfoBottomSheetScreenPreview() {
    PreviewAppTheme {
        DigaContributionInfoBottomSheetScreenContent()
    }
}
