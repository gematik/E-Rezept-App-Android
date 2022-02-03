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

package de.gematik.ti.erp.app.profiles.usecase

import androidx.paging.PagingData
import androidx.paging.map
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken.AlternateAuthenticationWithoutToken
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.DEFAULT_PROFILE_NAME
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ProfilesUseCase @Inject constructor(
    private val profilesRepository: ProfilesRepository,
    private val idpRepository: IdpRepository,
    dispatchProvider: DispatchProvider
) {

    @OptIn(FlowPreview::class)
    val profiles: Flow<List<ProfilesUseCaseData.Profile>> =
        profilesRepository.activeProfile().filterNotNull().flatMapLatest { activeProfile ->
            profilesRepository.profiles().transformLatest { profiles ->
                val profileFlows = profiles
                    .map { profile ->
                        combine(
                            idpRepository.getSingleSignOnToken(profile.name),
                            idpRepository.decryptedAccessToken(profile.name)
                        ) { ssoToken, accessToken ->
                            val active = activeProfile.profileName == profile.name
                            ProfilesUseCaseData.Profile(
                                profile.id,
                                profile.name,
                                ProfilesUseCaseData.ProfileInsuranceInformation(
                                    profile.insurantName,
                                    profile.insuranceIdentifier,
                                    profile.insuranceName
                                ),
                                active,
                                profile.color,
                                profile.lastAuthenticated,
                                ssoToken = ssoToken,
                                accessToken = accessToken,
                            )
                        }
                    }

                emitAll(combine(profileFlows) { it.toList() })
            }
        }
            .distinctUntilChanged()
            .shareIn(
                CoroutineScope(dispatchProvider.default()),
                SharingStarted.Lazily,
                1
            )
            .onEach { profiles ->
                profiles.forEach {
                    if (it.ssoToken != null &&
                        it.ssoToken !is AlternateAuthenticationWithoutToken &&
                        it.lastAuthenticated == null
                    ) {
                        updateLastAuthenticated(it.ssoToken.validOn, it.name)
                    }
                }
            }

    private suspend fun updateLastAuthenticated(validOn: Instant, profileName: String) =
        profilesRepository.updateLastAuthenticated(validOn, profileName)

    suspend fun addProfile(profileName: String, activate: Boolean = false) {
        if (profileName.isNotBlank()) {
            profilesRepository.saveProfile(profileName.trim(), activate = activate)
        }
    }

    /**
     * Removes the [profile] and adds a new profile with the name set to [newProfileName].
     */
    suspend fun removeProfile(profile: ProfilesUseCaseData.Profile, newProfileName: String) {
        addProfile(newProfileName, activate = true)

        idpRepository.invalidateDecryptedAccessToken(profile.name)
        profilesRepository.removeProfile(profile.name)
    }

    /**
     * Removes the [profile].
     */
    suspend fun removeProfile(profile: ProfilesUseCaseData.Profile) {
        idpRepository.invalidateDecryptedAccessToken(profile.name)
        profilesRepository.removeProfile(profile.name)
    }

    suspend fun logout(profile: ProfilesUseCaseData.Profile) {
        idpRepository.invalidateWithUserCredentials(profile.name)
    }

    fun isProfileSetupCompleted() =
        activeProfileName().map {
            it != DEFAULT_PROFILE_NAME
        }

    suspend fun overwriteDefaultProfileName(newProfileName: String) {
        profilesRepository.updateProfileByName(DEFAULT_PROFILE_NAME, newProfileName.trim(), activate = true)
    }

    fun isCanAvailable(profile: ProfilesUseCaseData.Profile) =
        idpRepository.cardAccessNumber(profile.name)
            .map { can ->
                can != null
            }

    suspend fun updateProfileName(profile: ProfilesUseCaseData.Profile, newProfileName: String) {
        val trimmedName = newProfileName.trim()
        if (trimmedName.isNotEmpty() && profile.name != trimmedName) {
            idpRepository.updateDecryptedAccessTokenMap(profile.name, trimmedName)
            profilesRepository.updateProfileByName(profile.name, trimmedName)
        }
    }

    suspend fun updateProfileColor(profile: ProfilesUseCaseData.Profile, color: ProfileColorNames) {
        profilesRepository.updateProfileColor(profile.name, color)
    }

    suspend fun switchActiveProfile(profile: ProfilesUseCaseData.Profile) {
        profilesRepository.saveProfile(profile.name, activate = true)
    }

    fun activeProfileName() = activeProfile().map { it.profileName }

    fun activeProfile() = profilesRepository.activeProfile()

    fun getProfileById(profileId: Int) = profilesRepository.getProfileById(profileId)

    suspend fun anyProfileAuthenticated() = profiles.first().any {
        it.lastAuthenticated != null
    }

    fun loadAuditEventsForProfile(profileName: String): Flow<PagingData<ProfilesUseCaseData.AuditEvent>> =
        profilesRepository.loadAuditEventsForProfile(profileName).map {
            it.map { auditEvent ->
                ProfilesUseCaseData.AuditEvent(
                    text = auditEvent.text,
                    timeStamp = auditEvent.timestamp,
                    medicationText = auditEvent.medicationText
                )
            }
        }
}
