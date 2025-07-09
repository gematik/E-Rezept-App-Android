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

package de.gematik.ti.erp.app.mainscreen.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun AddProfileChip(
    onClickAddProfile: () -> Unit
) {
    val shape = RoundedCornerShape(SizeDefaults.one)
    val description = stringResource(id = R.string.add_profile)
    Surface(
        modifier = Modifier
            .clip(shape)
            .clickable {
                onClickAddProfile()
            }
            .semantics {
                role = Role.Button
                contentDescription = description
            }
            .height(IntrinsicSize.Max)
            .testTag(TestTag.Main.AddProfileButton),
        shape = shape,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
    ) {
        Row(
            modifier = Modifier.padding(vertical = SizeDefaults.threeQuarter, horizontal = PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(SizeDefaults.triple),
                tint = AppTheme.colors.primary700
            )
        }
        // empty text to achieve same height as profile chips
        Text(
            text = "",
            style = AppTheme.typography.subtitle2,
            modifier = Modifier.padding(vertical = SizeDefaults.one, horizontal = PaddingDefaults.ShortMedium)
        )
    }
}
