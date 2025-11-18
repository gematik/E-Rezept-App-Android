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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

class PrescriptionDetailBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = false) {
    @Composable
    override fun Content() {
        val titleId =
            remember {
                navBackStackEntry.arguments?.getInt(PrescriptionDetailRoutes.BOTTOM_SHEET_TITLE_ID)
            } ?: return
        val infoId =
            remember {
                navBackStackEntry.arguments?.getInt(PrescriptionDetailRoutes.BOTTOM_SHEET_INFO_ID)
            } ?: return

        Column(
            Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge)
        ) {
            SpacerMedium()
            Text(
                stringResource(id = titleId),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900,
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.PrescriptionDetailBottomSheetTitle)
            )
            SpacerMedium()
            Box(
                Modifier
                    .verticalScroll(
                        rememberScrollState()
                    ).testTag(TestTag.Prescriptions.Details.PrescriptionDetailBottomSheetDetail)
            ) {
                Text(
                    stringResource(id = infoId),
                    style = AppTheme.typography.body2l
                )
            }
        }
    }
}
