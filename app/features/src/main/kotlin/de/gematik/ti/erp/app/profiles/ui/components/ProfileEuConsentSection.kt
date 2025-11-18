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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun ProfileEuConsentSection(
    euConsentStatus: Boolean?,
    onEuConsentClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
        Text(
            text = stringResource(R.string.eu_consent_text),
            style = AppTheme.typography.h6,
            modifier = Modifier
                .semanticsHeading()
                .padding(horizontal = PaddingDefaults.Medium)
        )
        ListItem(
            colors = GemListItemDefaults.gemListItemColors(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEuConsentClick, role = Role.Button),
            headlineContent = {
                Text(stringResource(R.string.eu_consent_countires_text), style = AppTheme.typography.body1)
            },
            leadingContent = {
                Icon(Icons.Rounded.Public, null, tint = AppTheme.colors.primary700)
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    when (euConsentStatus) {
                        true -> {
                            Text(
                                text = stringResource(R.string.eu_consent_staus_text_accept),
                                style = AppTheme.typography.body2,
                                color = AppTheme.colors.neutral500
                            )
                        }
                        false -> {
                            Text(
                                text = stringResource(R.string.eu_consent_staus_text_decline),
                                style = AppTheme.typography.body2,
                                color = AppTheme.colors.neutral500
                            )
                        }
                        null -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(SizeDefaults.oneHalf),
                                strokeWidth = SizeDefaults.quarter,
                                color = AppTheme.colors.neutral400
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        null,
                        tint = AppTheme.colors.neutral400
                    )
                }
            }
        )
    }
}

@LightDarkPreview
@Composable
fun ProfileEditConsentSectionPreview() {
    PreviewAppTheme {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)) {
            ProfileEuConsentSection(
                euConsentStatus = true,
                onEuConsentClick = { }
            )
        }
    }
}
