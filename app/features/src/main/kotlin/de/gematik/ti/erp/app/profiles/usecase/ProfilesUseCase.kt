/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

fun List<ProfilesUseCaseData.Profile>.activeProfile() =
    find { profile -> profile.active }!!

// TODO: Used only in test and debug-viewmodel. Remove it from there too.
class ProfilesUseCase(
    private val profilesRepository: ProfileRepository,
    private val idpRepository: IdpRepository
) {

    val profiles: Flow<List<ProfilesUseCaseData.Profile>>
        get() = profilesRepository.profiles().map { profiles ->
            profiles.map { profile ->
                ProfilesUseCaseData.Profile(
                    id = profile.id,
                    name = profile.name,
                    insurance = ProfileInsuranceInformation(
                        insurantName = profile.insurantName ?: "",
                        insuranceIdentifier = profile.insuranceIdentifier ?: "",
                        insuranceName = profile.insuranceName ?: "",
                        insuranceType = when (profile.insuranceType) {
                            ProfilesData.InsuranceType.None -> ProfilesUseCaseData.InsuranceType.NONE
                            ProfilesData.InsuranceType.GKV -> ProfilesUseCaseData.InsuranceType.GKV
                            ProfilesData.InsuranceType.PKV -> ProfilesUseCaseData.InsuranceType.PKV
                        }
                    ),
                    active = profile.active,
                    color = profile.color,
                    avatar = profile.avatar,
                    image = profile.personalizedImage,
                    lastAuthenticated = profile.lastAuthenticated,
                    ssoTokenScope = profile.singleSignOnTokenScope
                )
            }
        }
            .distinctUntilChanged()
            .onEach { profiles ->
                profiles.forEach { profile ->
                    if (profile.ssoTokenScope != null &&
                        profile.ssoTokenScope !is IdpData.AlternateAuthenticationWithoutToken &&
                        profile.lastAuthenticated == null
                    ) {
                        profile.ssoTokenScope?.token?.let { token ->
                            profilesRepository.updateLastAuthenticated(profile.id, token.validOn)
                        }
                    }
                }
            }

    val activeProfile: Flow<ProfilesUseCaseData.Profile> =
        profiles.map { it.activeProfile() }

    suspend fun addProfile(newProfileName: String, activate: Boolean = false) {
        sanitizedProfileName(newProfileName)?.also { profileName ->
            profilesRepository.saveProfile(profileName.trim(), activate = activate)
        } ?: error("invalid profile name `$newProfileName`")
    }

    /**
     * Removes the [profile] and adds a new profile with the name set to [newProfileName].
     */
    suspend fun removeAndSaveProfile(profile: ProfilesUseCaseData.Profile, newProfileName: String) {
        addProfile(newProfileName, activate = true)

        idpRepository.invalidateDecryptedAccessToken(profile.name)
        profilesRepository.removeProfile(profile.id)
    }

    /**
     * Removes the [profile].
     */
    suspend fun removeProfile(profile: ProfilesUseCaseData.Profile) {
        idpRepository.invalidateDecryptedAccessToken(profile.name)
        profilesRepository.removeProfile(profile.id)
    }

    @Requirement(
        "O.Tokn_6#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "invalidate config and token"
    )
    suspend fun logout(profile: ProfilesUseCaseData.Profile) {
        idpRepository.invalidate(profile.id)
    }

    suspend fun updateProfileName(profileId: ProfileIdentifier, newProfileName: String) {
        sanitizedProfileName(newProfileName)?.also { profileName ->
            profilesRepository.updateProfileName(profileId, profileName)
        } ?: error("invalid profile name `$newProfileName`")
    }

    suspend fun updateProfileColor(profile: ProfilesUseCaseData.Profile, color: ProfilesData.ProfileColorNames) {
        profilesRepository.updateProfileColor(profile.id, color)
    }

    // tag::SwitchActiveProfileUseCase[]
    suspend fun switchActiveProfile(profile: ProfilesUseCaseData.Profile) {
        profilesRepository.activateProfile(profile.id)
    }
    // end::SwitchActiveProfileUseCase[]

    fun activeProfileId() = activeProfile().mapNotNull { it!!.id }

    fun activeProfile() = profilesRepository.profiles().map {
        it.find { profile ->
            profile.active
        }
    }

    suspend fun switchProfileToPKV(profileId: ProfileIdentifier) {
        profilesRepository.switchProfileToPKV(profileId)
    }
}

fun sanitizedProfileName(profileName: String): String? =
    if (profileName.isNotBlank()) profileName.trim() else null
