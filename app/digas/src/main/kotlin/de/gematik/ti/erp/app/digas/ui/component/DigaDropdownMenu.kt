/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun DigaDropdownMenu(
    isDeletable: Boolean = false,
    isArchivable: Boolean = false,
    isRedeemableAgain: Boolean,
    isReadyForRequest: Boolean = false,
    isArchiveRevertable: Boolean = false,
    onClickDelete: () -> Unit,
    onClickArchive: () -> Unit = {},
    onClickRedeemAgain: () -> Unit = {},
    onClickRevertArchive: () -> Unit = {},
    onClickRefresh: () -> Unit = {}
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.diga_show_actions)

    IconButton(
        onClick = { isMenuExpanded = true },
        modifier = Modifier
            .testTag(TestTag.Prescriptions.Details.MoreButton)
            .semantics { contentDescription = description }
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false },
        offset = DpOffset(SizeDefaults.threeSeventyFifth, SizeDefaults.zero)
    ) {
        if (isReadyForRequest && !isRedeemableAgain) {
            DigaDropdownMenuItem(
                text = stringResource(R.string.request_code),
                testTag = TestTag.Digas.DigaMenuSend,
                isMenuExpanded = { isMenuExpanded = it },
                onClick = onClickRefresh

            )
        }
        if (isRedeemableAgain) {
            DigaDropdownMenuItem(
                text = stringResource(R.string.diga_menu_send_again),
                testTag = TestTag.Digas.DigaMenuSendAgain,
                isMenuExpanded = { isMenuExpanded = it },
                onClick = onClickRedeemAgain

            )
        }
        if (isArchivable) {
            DigaDropdownMenuItem(
                text = stringResource(R.string.archive),
                testTag = TestTag.Digas.DigaMenuArchive,
                isMenuExpanded = { isMenuExpanded = it },
                onClick = onClickArchive
            )
        }

        if (isArchiveRevertable) {
            DropdownMenuItem(
                modifier = Modifier
                    .testTag(TestTag.Prescriptions.Details.DeleteButton)
                    .semantics { role = Role.Button },
                onClick = {
                    isMenuExpanded = false
                    onClickRevertArchive()
                }
            ) {
                Text(
                    text = stringResource(R.string.archive_revert),
                    color = AppTheme.colors.neutral900
                )
            }
        }
        if (isDeletable) {
            DigaDropdownMenuItem(
                text = stringResource(R.string.pres_detail_dropdown_delete),
                testTag = TestTag.Digas.DigaMenuDelete,
                color = AppTheme.colors.red600,
                isMenuExpanded = { isMenuExpanded = it },
                onClick = onClickDelete

            )
        }
    }
}

@Composable
private fun DigaDropdownMenuItem(
    text: String,
    testTag: String,
    color: Color = AppTheme.colors.neutral900,
    isMenuExpanded: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = Modifier
            .testTag(testTag)
            .semantics { role = Role.Button },
        onClick = {
            isMenuExpanded(false)
            onClick()
        }
    ) {
        Text(
            text = text,
            color = color
        )
    }
}
