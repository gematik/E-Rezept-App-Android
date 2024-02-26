/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.troubleshooting.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny

@Composable
fun TroubleShootingInfo(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.cdw_enter_troubleshooting_title),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.cdw_enter_troubleshooting_subtitle),
            style = AppTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        SpacerMedium()
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(SizeDefaults.one),
            elevation = ButtonDefaults.elevation(defaultElevation = SizeDefaults.zero),
            contentPadding = PaddingValues(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Tiny)
        ) {
            Icon(Icons.Outlined.Lightbulb, null)
            SpacerTiny()
            Text(stringResource(R.string.cdw_enter_troubleshooting_action))
        }
    }
}
