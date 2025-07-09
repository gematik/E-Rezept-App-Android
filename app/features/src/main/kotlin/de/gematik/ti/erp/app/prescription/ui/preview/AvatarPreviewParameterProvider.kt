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

package de.gematik.ti.erp.app.prescription.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

data class AvatarPreview(
    val description: String,
    val profileIconState: ProfileIconState,
    val activeProfile: ProfilesUseCaseData.Profile,
    val isRegistered: Boolean,
    val isTokenValid: Boolean
)

class AvatarPreviewParameterProvider : PreviewParameterProvider<AvatarPreview> {
    override val values: Sequence<AvatarPreview>
        get() = sequenceOf(
            AvatarPreview(
                description = "IsOffline",
                profileIconState = ProfileIconState.IsOffline,
                isRegistered = true,
                isTokenValid = false,
                activeProfile = PREVIEW_INVALID_PROFILE
            ),
            AvatarPreview(
                description = "IsOnline",
                profileIconState = ProfileIconState.IsOnline,
                isRegistered = true,
                isTokenValid = true,
                activeProfile = PREVIEW_ACTIVE_PROFILE
            ),
            AvatarPreview(
                description = "IsError",
                profileIconState = ProfileIconState.IsError,
                isRegistered = true,
                isTokenValid = true,
                activeProfile = PREVIEW_INVALID_PROFILE
            )
        )
}
