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

package de.gematik.ti.erp.app.pkv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Composable
fun InvoiceThreeDotMenu(
    taskId: String,
    onClickShareInvoice: (String) -> Unit,
    onClickRemoveInvoice: (String) -> Unit,
    onClickCorrectInvoiceLocally: (String) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isDropDownExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { isMenuExpanded = true }
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false },
        offset = DpOffset(24.dp, 0.dp)
    ) {
        DropdownMenuItem(
            onClick = {
                onClickShareInvoice(taskId)
                isMenuExpanded = false
            }
        ) {
            Text(
                text = stringResource(R.string.invoice_header_share)
            )
        }
        if (isDropDownExpanded) {
            SpacerSmall()
        }
        DropdownMenuItem(
            onClick = {
                isDropDownExpanded = !isDropDownExpanded
            }
        ) {
            Column {
                val arrow = when (isDropDownExpanded) {
                    true -> Icons.Outlined.KeyboardArrowDown
                    false -> Icons.Outlined.KeyboardArrowRight
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.invoice_menu_correct_invoice))
                    SpacerSmall()
                    Icon(arrow, null, tint = AppTheme.colors.neutral400)
                }

                if (isDropDownExpanded) {
                    DropdownMenuItem(onClick = {
                        onClickCorrectInvoiceLocally(taskId)
                        isMenuExpanded = false
                    }) {
                        Text(
                            text = stringResource(R.string.invoice_menu_correct_invoice_locally),
                            style = AppTheme.typography.subtitle1
                        )
                    }
                    DropdownMenuItem(onClick = {}) {
                        Text(
                            text = stringResource(R.string.invoice_menu_correct_invoice_online),
                            style = AppTheme.typography.subtitle1l
                        )
                    }
                }
            }
        }
        DropdownMenuItem(
            onClick = {
                onClickRemoveInvoice(taskId)
                isMenuExpanded = false
            }
        ) {
            Text(
                text = stringResource(R.string.invoice_header_delete),
                color = AppTheme.colors.red600
            )
        }
    }
}
