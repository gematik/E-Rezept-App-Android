package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.entities.ProfileColors
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.DEFAULT_PROFILE_NAME
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
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ProfilesUseCase @Inject constructor(
    private val profilesRepository: ProfilesRepository,
    private val idpRepository: IdpRepository,
    dispatchProvider: DispatchProvider
) {
    fun demoProfiles() = profilesRepository.demoProfiles

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
                    if (it.ssoToken != null && it.lastAuthenticated == null) {
                        updateLastAuthenticated(it.ssoToken.validOn, it.name)
                    }
                }
            }

    suspend fun addProfile(profileName: String) {
        if (profileName.isNotBlank()) {
            val trimmedName = profileName.trim()
            profilesRepository.saveProfile(trimmedName)
        }
    }

    suspend fun addDefaultProfile() {
        profilesRepository.saveProfile(DEFAULT_PROFILE_NAME)
        insertActiveProfile(DEFAULT_PROFILE_NAME)
    }

    suspend fun removeProfile(profileName: String, newProfileName: String?) {
        if (newProfileName != null) {
            addProfile(newProfileName)
            insertActiveProfile(newProfileName)
        }
        profilesRepository.removeLastModifiedTaskDate(profileName)
        idpRepository.invalidateDecryptedAccessToken(profileName)
        profilesRepository.removeProfile(profileName)
    }

    suspend fun logout(profile: ProfilesUseCaseData.Profile) {
        idpRepository.invalidateWithUserCredentials(profile.name)
    }

    fun isProfileSetupCompleted() =
        activeProfileName().map {
            it != DEFAULT_PROFILE_NAME
        }

    suspend fun overwriteDefaultProfileName(profileName: String) {
        val trimmedName = profileName.trim()
        switchActiveProfileOnUpdateActiveProfileName(DEFAULT_PROFILE_NAME, trimmedName)
        profilesRepository.updateProfileName(DEFAULT_PROFILE_NAME, trimmedName)
    }

    suspend fun activateProfile(profileName: String) {
        insertActiveProfile(profileName)
    }

    fun isCanAvailable(profile: ProfilesUseCaseData.Profile) =
        idpRepository.cardAccessNumber(profile.name)
            .map { can ->
                can != null
            }

    suspend fun updateProfile(profile: ProfilesUseCaseData.Profile, newName: String) {
        if (newName.isNotBlank()) {
            val trimmedName = newName.trim()
            idpRepository.updateDecryptedAccessTokenMap(profile.name, trimmedName)
            switchActiveProfileOnUpdateActiveProfileName(profile.name, trimmedName)
            profilesRepository.updateProfile(profile.id, trimmedName)
        }
    }

    private suspend fun switchActiveProfileOnUpdateActiveProfileName(
        profileName: String,
        trimmedName: String
    ) {
        if (activeProfile().filterNotNull()
            .first().profileName == profileName
        ) {
            switchActiveProfile(trimmedName)
        }
    }

    suspend fun updateProfileColor(profileName: String, color: ProfileColors) {
        profilesRepository.updateProfileColor(profileName, color)
    }

    suspend fun insertActiveProfile(profileName: String) {
        profilesRepository.insertActiveProfile(profileName)
    }

    suspend fun switchActiveProfile(profileName: String) {
        profilesRepository.updateActiveProfileName(profileName)
    }

    fun activeProfileName() =
        activeProfile().map {
            it?.profileName ?: error("no active profile available")
        }

    suspend fun setupProfile(profileName: String) {
        addProfile(profileName)
        insertActiveProfile(profileName)
    }

    fun activeProfile() = profilesRepository.activeProfile()

    fun getProfileById(profileId: Int) = profilesRepository.getProfileById(profileId)

    suspend fun updateLastAuthenticated(validOn: Instant, profileName: String) =
        profilesRepository.updateLastAuthenticated(validOn, profileName)

    suspend fun anyProfileAuthenticated() = profiles.first().any {
        it.lastAuthenticated != null
    }
}
