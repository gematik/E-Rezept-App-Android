/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.profiles.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfileAvatarUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

import kotlinx.coroutines.launch

class ProfileSettingsViewModel(
    private val profilesUseCase: ProfilesUseCase,
    private val profileAvatarUseCase: ProfileAvatarUseCase
) : ViewModel() {

    fun updateProfileColor(profile: ProfilesUseCaseData.Profile, color: ProfilesData.ProfileColorNames) {
        viewModelScope.launch {
            profilesUseCase.updateProfileColor(profile, color)
        }
    }

    fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: Bitmap) {
        viewModelScope.launch {
            profileAvatarUseCase.savePersonalizedProfileImage(profileId, profileImage)
        }
    }

    fun updateProfileName(profile: ProfilesUseCaseData.Profile, newName: String) {
        viewModelScope.launch {
            profilesUseCase.updateProfileName(profile, newName)
        }
    }

    fun saveAvatarFigure(profileId: ProfileIdentifier, avatarFigure: ProfilesData.AvatarFigure) {
        viewModelScope.launch {
            profileAvatarUseCase.saveAvatarFigure(profileId, avatarFigure)
        }
    }

    fun clearPersonalizedImage(profileId: ProfileIdentifier) {
        viewModelScope.launch {
            profileAvatarUseCase.clearPersonalizedImage(profileId)
        }
    }
}
