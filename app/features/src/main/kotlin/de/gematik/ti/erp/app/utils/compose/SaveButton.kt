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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun SaveButton(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.generic_save),
    isEnabled: Boolean,
    onEmpty: () -> Unit,
    onClick: () -> Unit
) {
    Center(
        modifier = Modifier.background(AppTheme.colors.neutral025)
    ) {
        Button(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = SizeDefaults.tenfold)
                .padding(vertical = PaddingDefaults.XXLargeMedium)
                .imePadding()
                .testTag(TestTag.Profile.EditProfileIcon.EmojiSaveButton),
            colors = erezeptButtonColors(),
            onClick = if (isEnabled) onClick else onEmpty
        ) {
            Text(
                color = AppTheme.colors.neutral000,
                textAlign = TextAlign.Center,
                text = text
            )
        }
    }
}
