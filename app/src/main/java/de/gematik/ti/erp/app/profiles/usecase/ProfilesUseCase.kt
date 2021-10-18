package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.db.entities.Profile
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.settings.usecase.DEFAULT_PROFILE_NAME
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ProfilesUseCase @Inject constructor(
    private val profilesRepository: ProfilesRepository,
) {

    fun profiles() = profilesRepository.profiles()

    suspend fun addProfile(profile: Profile) {
        profilesRepository.saveProfile(profile)
    }

    suspend fun deleteProfile(profile: Profile) {
        profilesRepository.deleteProfile(profile)
    }

    // TODO: if user removed the last profile we suggest complete logout which means reset of the app???
    suspend fun removeProfile(profileName: String) {
        profilesRepository.removeProfile(profileName)
        if (profileName == activeProfileName().first()) {
            if (profiles().firstOrNull() == null) {
                addProfile(generateDefaultProfile())
            }
            insertActiveProfile(profiles().first()[0].name)
        }
    }

    suspend fun updateProfile(profileId: Int, profileName: String) {
        if (activeProfile().filterNotNull()
            .first().profileName == getProfileById(profileId)
                .filterNotNull().first().name
        ) {
            insertActiveProfile(profileName)
        }
        profilesRepository.updateProfile(profileId, profileName)
    }

    suspend fun updateProfileName(currentName: String, updatedName: String) {
        profilesRepository.updateProfileName(currentName, updatedName)
    }

    suspend fun insertActiveProfile(profileName: String) {
        profilesRepository.insertActiveProfile(profileName)
    }

    fun activeProfileName() =
        activeProfile().map {
            it?.profileName ?: error("no active profile available")
        }

    suspend fun setupProfile(profileName: String) {
        val profile = Profile(id = 0, name = profileName, insuranceNumber = "")
        addProfile(profile)
        insertActiveProfile(profile.name)
    }

    fun generateDefaultProfile() = Profile(
        id = 0,
        name = DEFAULT_PROFILE_NAME,
        insuranceNumber = ""
    )

    fun activeProfile() = profilesRepository.activeProfile()
    fun getProfileById(profileId: Int) = profilesRepository.getProfileById(profileId)
}
