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

package de.gematik.ti.erp.app.medicationplan.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileHeader(profile: ProfilesUseCaseData.Profile) {
    ListItem(
        modifier = Modifier,
        icon = {
            Avatar(
                modifier = Modifier.size(SizeDefaults.sixfold),
                emptyIcon = Icons.Rounded.PersonOutline,
                profile = profile,
                iconModifier = Modifier.size(SizeDefaults.doubleHalf)
            )
        },
        text = {
            Text(
                text = profile.name,
                style = AppTheme.typography.body1
            )
        }
    )
}

@LightDarkPreview
@Composable
private fun ProfileHeaderPreview() {
    PreviewAppTheme {
        ProfileHeader(
            profile = ProfilesUseCaseData.Profile(
                id = "1",
                name = "Max Mustermann",
                insurance = ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                ),
                isActive = true,
                color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                lastAuthenticated = null,
                ssoTokenScope = null,
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null
            )
        )
    }
}
