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

package de.gematik.ti.erp.app.profiles.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewData.PharmacyOrders
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewData.emptyPharmacyOrders
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock

class PrescriptionPreviewParameterProvider : PreviewParameterProvider<List<PharmacyUseCaseData.PrescriptionInOrder>> {

    override val values: Sequence<List<PharmacyUseCaseData.PrescriptionInOrder>>
        get() = sequenceOf(
            PharmacyOrders,
            emptyPharmacyOrders
        )
}

val gkvProfile = ProfilesUseCaseData.Profile(
    id = "1",
    name = "Max Mustermann",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    lastAuthenticated = Clock.System.now(),
    ssoTokenScope = null,
    insurance = ProfileInsuranceInformation(
        insurantName = "Max Mustermann",
        insuranceName = "TK",
        insuranceIdentifier = "123456789",
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true
)

val emptyGkvProfile = ProfilesUseCaseData.Profile(
    id = "",
    name = "",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    lastAuthenticated = null,
    ssoTokenScope = null,
    insurance = ProfileInsuranceInformation(
        insurantName = "",
        insuranceName = "",
        insuranceIdentifier = "",
        insuranceType = ProfilesUseCaseData.InsuranceType.NONE
    ),
    isActive = false
)

val neverAuthenticatedGkvProfile = ProfilesUseCaseData.Profile(
    id = "1",
    name = "Profile 1",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    lastAuthenticated = null,
    ssoTokenScope = null,
    insurance = ProfileInsuranceInformation(
        insurantName = "",
        insuranceName = "",
        insuranceIdentifier = "",
        insuranceType = ProfilesUseCaseData.InsuranceType.NONE
    ),
    isActive = true
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
    insurance = ProfileInsuranceInformation(
        insurantName = "Max Mustermann",
        insuranceName = "TK",
        insuranceIdentifier = "123456789",
        insuranceType = ProfilesUseCaseData.InsuranceType.PKV
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

val profileMissingImage = gkvProfile.copy(avatar = ProfilesData.Avatar.PersonalizedImage)

val profileMissingImageState = UiState(
    data = ProfileCombinedData(
        selectedProfile = profileMissingImage,
        profiles = listOf(
            profileMissingImage
        )
    )
)
