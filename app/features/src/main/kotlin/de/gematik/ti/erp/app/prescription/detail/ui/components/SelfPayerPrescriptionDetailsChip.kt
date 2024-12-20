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

package de.gematik.ti.erp.app.prescription.detail.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.prescription.ui.StatusChip
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview

@Composable
fun SelfPayPrescriptionDetailsChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) =
    StatusChip(
        modifier = modifier
            .clickable(role = Role.Button) {
                onClick()
            },
        text = stringResource(R.string.pres_details_exp_sel_payer_prescription),
        icon = Icons.Outlined.Info,
        textColor = AppTheme.colors.primary900,
        backgroundColor = AppTheme.colors.primary100,
        iconColor = AppTheme.colors.primary600
    )

@LightDarkPreview
@Composable
fun PreviewSelfPayPrescriptionDetailsChip() {
    SelfPayPrescriptionDetailsChip(
        onClick = {}
    )
}
