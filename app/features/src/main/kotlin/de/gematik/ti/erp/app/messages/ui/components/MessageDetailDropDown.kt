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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.dropdown.GemDropdownMenuItem
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun MessageDetailDropdownMenu(
    isTranslationAllowed: Boolean,
    onClickChangeConsent: () -> Unit
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
        GemDropdownMenuItem(
            text = when {
                isTranslationAllowed -> stringResource(R.string.offline_translation_menu_deny_translation)
                else -> stringResource(R.string.offline_translation_menu_allow_translation)
            },
            testTag = TestTag.OfflineTranslation.OfflineTranslationMenuSelect,
            isMenuExpanded = { isMenuExpanded = it },
            onClick = onClickChangeConsent

        )
    }
}
