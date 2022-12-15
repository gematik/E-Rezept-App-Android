/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.redeem.ui.rememberRedeemController
import de.gematik.ti.erp.app.utils.compose.AcceptDialog

@Composable
fun RedeemFloatingActionButton(
    onClick: () -> Unit
) {
    val redeemState = rememberRedeemController()
    val hasRedeemableTasks by redeemState.hasRedeemableTasks

    var showNoRedeemableDialog by remember { mutableStateOf(false) }
    if (showNoRedeemableDialog) {
        AcceptDialog(
            header = stringResource(R.string.main_redeem_no_rx_title),
            info = stringResource(R.string.main_redeem_no_rx_desc),
            acceptText = stringResource(R.string.ok),
            onClickAccept = {
                showNoRedeemableDialog = false
            }
        )
    }

    ExtendedFloatingActionButton(
        modifier = Modifier.heightIn(min = 56.dp),
        text = { Text(stringResource(R.string.main_redeem_button)) },
        shape = RoundedCornerShape(16.dp),
        icon = { Icon(Icons.Rounded.Upload, null) },
        onClick = {
            if (hasRedeemableTasks) {
                onClick()
            } else {
                showNoRedeemableDialog = true
            }
        }
    )
}
