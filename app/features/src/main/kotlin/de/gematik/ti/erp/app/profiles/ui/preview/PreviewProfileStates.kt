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

package de.gematik.ti.erp.app.profiles.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewData.PharmacyOrders
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewData.emptyPharmacyOrders
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock

class PrescriptionPreviewParameterProvider : PreviewParameterProvider<List<PrescriptionInOrderErpModel>> {

    override val values: Sequence<List<PrescriptionInOrderErpModel>>
        get() = sequenceOf(
            PharmacyOrders,
            emptyPharmacyOrders
        )
}

val gkvProfile = ProfileErpModel(
    id = "1",
    name = "Max Mustermann",
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = null
    ),
    lastAuthenticated = Clock.System.now(),
    userAuthentication = UserAuthenticationErpModel.NotInitialized,
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "Max Mustermann",
        insuranceName = "TK",
        insuranceIdentifier = "123456789",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = null
    ),
    active = true,
    isNewlyCreated = false,
    isConsentDrawerShown = true,
    lastTaskSynced = null,
    lastAuditEventSynced = null
)

val emptyGkvProfile = ProfileErpModel(
    id = "",
    name = "",
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = null
    ),
    lastAuthenticated = null,
    userAuthentication = UserAuthenticationErpModel.NotInitialized,
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "",
        insuranceName = "",
        insuranceIdentifier = "",
        insuranceType = InsuranceType.NONE,
        organizationIdentifier = null
    ),
    active = false,
    isNewlyCreated = false,
    isConsentDrawerShown = false,
    lastAuditEventSynced = null,
    lastTaskSynced = null
)

val neverAuthenticatedGkvProfile = ProfileErpModel(
    id = "1",
    name = "Profile 1",
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = null
    ),
    lastAuthenticated = null,
    userAuthentication = UserAuthenticationErpModel.NotInitialized,
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "",
        insuranceName = "",
        insuranceIdentifier = "",
        insuranceType = InsuranceType.NONE,
        organizationIdentifier = null
    ),
    active = true,
    isNewlyCreated = false,
    isConsentDrawerShown = false,
    lastAuditEventSynced = null,
    lastTaskSynced = null
)

val gkvProfileState = UiState(
    data = ProfileCombinedData(
        selectedProfile = gkvProfile,
        profiles = listOf(
            gkvProfile
        )
    )
)

val neverAuthenticatedGkvProfileState = UiState(
    data = ProfileCombinedData(
        selectedProfile = emptyGkvProfile,
        profiles = listOf(
            emptyGkvProfile
        )
    )
)

val pkvProfile = gkvProfile.copy(
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "Max Mustermann",
        insuranceName = "TK",
        insuranceIdentifier = "123456789",
        insuranceType = InsuranceType.PKV,
        organizationIdentifier = null
    )
)

val pkvProfileState = UiState(
    data = ProfileCombinedData(
        selectedProfile = pkvProfile,
        profiles = listOf(
            pkvProfile
        )
    )
)

val profileMissingImage = gkvProfile.copy(
    profileImageData = gkvProfile.profileImageData.copy(avatar = Avatar.PersonalizedImage)
)

val profileMissingImageState = UiState(
    data = ProfileCombinedData(
        selectedProfile = profileMissingImage,
        profiles = listOf(
            profileMissingImage
        )
    )
)
