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
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileHeader(profile: ProfileErpModel) {
    ListItem(
        modifier = Modifier,
        icon = {
            Avatar(
                modifier = Modifier.size(SizeDefaults.sixfold),
                emptyIcon = Icons.Rounded.PersonOutline,
                imageData = profile.profileImageData,
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
            profile = ProfileErpModel(
                id = "1",
                name = "Max Mustermann",
                insuranceData = ProfileInsuranceDataErpModel(
                    insuranceType = InsuranceType.GKV,
                    insuranceIdentifier = "123456789",
                    insurantName = "Max Mustermann",
                    insuranceName = "GKV",
                    organizationIdentifier = null
                ),
                active = true,
                profileImageData = ProfileImageDataErpModel(
                    color = ProfileColorNames.SPRING_GRAY,
                    avatar = Avatar.PersonalizedImage,
                    image = null
                ),
                lastAuthenticated = null,
                userAuthentication = UserAuthenticationErpModel.NotInitialized,
                lastTaskSynced = null,
                lastAuditEventSynced = null,
                isConsentDrawerShown = true,
                isNewlyCreated = false
            )
        )
    }
}
