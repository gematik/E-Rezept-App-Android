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
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.ui.preview.ProfilePreviewParameterData.emptyGkvProfileError
import de.gematik.ti.erp.app.profiles.ui.preview.ProfilePreviewParameterData.existingGkvProfile
import de.gematik.ti.erp.app.profiles.ui.preview.ProfilePreviewParameterData.newGkvProfile
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState

class ProfileStatePreviewParameterProvider : PreviewParameterProvider<UiState<ProfileCombinedData>> {

    override val values = sequenceOf(
        UiState.Loading(),
        UiState.Empty(),
        UiState.Error(Throwable("Error")),
        gkvProfileState,
        pkvProfileState,
        profileMissingImageState
    )
}

data class ProfileEditData(
    val profile: ProfilesUseCaseData.Profile,
    val initialDuplicated: Boolean = false,
    val initialHasUserInteracted: Boolean = false
)

class ProfilePreviewParameterProvider : PreviewParameterProvider<ProfileEditData> {

    override val values: Sequence<ProfileEditData> = sequenceOf(
        newGkvProfile,
        existingGkvProfile,
        emptyGkvProfileError
    )
}

object ProfilePreviewParameterData {

    var newGkvProfile: ProfileEditData = ProfileEditData(
        gkvProfile,
        false
    )
    var existingGkvProfile = ProfileEditData(
        gkvProfile,
        true,
        true
    )

    var emptyGkvProfileError: ProfileEditData = ProfileEditData(
        emptyGkvProfile,
        false,
        true
    )
}
