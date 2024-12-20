/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium

@Composable
fun PromptScaffold(
    title: String,
    profile: ProfilesUseCaseData.Profile?,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp
    ) {
        Column(
            Modifier
                .padding(vertical = PaddingDefaults.Medium)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                profile?.let {
                    Avatar(
                        modifier = Modifier.size(36.dp),
                        emptyIcon = Icons.Rounded.PersonOutline,
                        iconModifier = Modifier.size(20.dp),
                        profile = profile
                    )
                    SpacerMedium()
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = AppTheme.typography.h6,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            profile.insurance.insuranceIdentifier,
                            style = AppTheme.typography.body2l
                        )
                    }
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cdw_nfc_dlg_cancel))
                }
            }
            SpacerLarge()
            content()
        }
    }
}
