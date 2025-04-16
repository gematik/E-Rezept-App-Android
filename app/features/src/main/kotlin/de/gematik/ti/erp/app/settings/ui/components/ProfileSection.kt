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

package de.gematik.ti.erp.app.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource

@Composable
fun ProfileSection(
    profiles: List<ProfilesUseCaseData.Profile>,
    onClickEditProfile: (ProfileIdentifier) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_profiles_headline),
            style = AppTheme.typography.h6,
            modifier = Modifier
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Small
                )
                .semanticsHeading()
                .testTag("Profiles")
        )

        profiles.forEach { profile ->
            ProfileCard(
                profile = profile,
                onClickEdit = { onClickEditProfile(profile.id) }
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ProfilesUseCaseData.Profile,
    onClickEdit: () -> Unit
) {
    val openProfileDescription = annotatedStringResource(R.string.open_profile_button, profile.name).toString()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = openProfileDescription
            ) {
                onClickEdit()
            }
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.ShortMedium)
            .testTag(TestTag.Settings.ProfileButton),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            modifier = Modifier.size(SizeDefaults.sixfold),
            emptyIcon = Icons.Rounded.PersonOutline,
            profile = profile,
            iconModifier = Modifier.size(SizeDefaults.doubleHalf)
        )
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = profile.name,
            style = AppTheme.typography.body1
        )
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}
