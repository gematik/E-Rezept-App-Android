/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.db.entities.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

fun List<ProfilesUseCaseData.Profile>.activeProfile() =
    find { profile -> profile.active }!!

class ProfilesUseCase(
    private val profilesRepository: ProfilesRepository,
    private val idpRepository: IdpRepository,
    private val auditRepository: AuditEventsRepository
) {

    val profiles: Flow<List<ProfilesUseCaseData.Profile>>
        get() = profilesRepository.profiles().map { profiles ->
            profiles.map { profile ->
                ProfilesUseCaseData.Profile(
                    id = profile.id,
                    name = profile.name,
                    insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(
                        insurantName = profile.insurantName ?: "",
                        insuranceIdentifier = profile.insuranceIdentifier ?: "",
                        insuranceName = profile.insuranceName ?: "",
                        insuranceType = when (profile.insuranceType) {
                            InsuranceTypeV1.None -> ProfilesUseCaseData.InsuranceType.NONE
                            InsuranceTypeV1.GKV -> ProfilesUseCaseData.InsuranceType.GKV
                            InsuranceTypeV1.PKV -> ProfilesUseCaseData.InsuranceType.PKV
                        }
                    ),
                    active = profile.active,
                    color = profile.color,
                    avatarFigure = profile.avatarFigure,
                    personalizedImage = profile.personalizedImage,
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
                        profile.ssoTokenScope.token?.let { token ->
                            profilesRepository.updateLastAuthenticated(profile.id, token.validOn)
                        }
                    }
                }
            }

    val activeProfile: Flow<ProfilesUseCaseData.Profile> =
        profiles.map { it.activeProfile() }

    fun decryptedAccessToken(profileId: ProfileIdentifier): Flow<String?> =
        idpRepository.decryptedAccessToken(profileId)

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

    fun auditEvents(profileId: ProfileIdentifier) = auditRepository.auditEvents(profileId)
    suspend fun switchProfileToPKV(profile: ProfilesUseCaseData.Profile) {
        profilesRepository.switchProfileToPKV(profile.id)
    }
}

fun sanitizedProfileName(profileName: String): String? =
    if (profileName.isNotBlank()) profileName.trim() else null
