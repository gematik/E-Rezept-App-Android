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

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny

@Composable
fun OverviewSupportRow(onShowSupportBottomSheet: () -> Unit) {
    OverviewRow(
        onClickLabel = stringResource(R.string.open),
        onClick = onShowSupportBottomSheet
    ) {
        Icon(imageVector = Icons.AutoMirrored.Outlined.ContactSupport, contentDescription = "")
        SpacerTiny()
        Text(text = stringResource(R.string.help_and_support), style = AppTheme.typography.body1)
        SpacerSmall()
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "",
            tint = AppTheme.colors.primary700
        )
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewSupportRowPreview() {
    PreviewTheme {
        OverviewSupportRow(onShowSupportBottomSheet = {})
    }
}
