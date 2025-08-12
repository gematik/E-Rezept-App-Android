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

package de.gematik.ti.erp.app.pkv.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

@Composable
fun InvoiceThreeDotMenu(
    taskId: String,
    onClickShareInvoice: (String) -> Unit,
    onClickRemoveInvoice: (String) -> Unit,
    onClickCorrectInvoiceLocally: (String) -> Unit,
    onClickCorrectInvoiceInApp: (String) -> Unit
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
        offset = DpOffset(SizeDefaults.triple, SizeDefaults.zero)
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
                    false -> Icons.AutoMirrored.Outlined.KeyboardArrowRight
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
                    DropdownMenuItem(onClick = {
                        onClickCorrectInvoiceInApp(taskId)
                    }) {
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
