/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.profiles.repository

import android.content.SharedPreferences
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.db.entities.ProfileColors
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import java.time.Instant
import javax.inject.Inject

class ProfilesRepository @Inject constructor(
    private val db: AppDatabase,
    @NetworkSecureSharedPreferences
    private var securePrefs: SharedPreferences
) {
    fun profiles() =
        db.profileDao().getAllProfiles()

    val demoProfiles = mutableListOf(
        ProfilesUseCaseData.Profile(
            id = 0,
            name = "Anna Vetter",
            active = false,
            color = ProfileColors.BLUE_MOON
        )
    )

    suspend fun saveProfile(profileName: String) {
        db.profileDao().insertProfile(ProfileEntity(name = profileName))
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        db.profileDao().deleteProfile(profile)
    }

    suspend fun insertActiveProfile(profileName: String) {
        db.activeProfileDao().insertActiveSession(ActiveProfile(profileName = profileName))
    }

    suspend fun updateActiveProfileName(profileName: String) {
        db.activeProfileDao().updateActiveProfile(profileName)
    }

    suspend fun removeProfile(profileName: String) {
        db.profileDao().removeProfile(profileName)
    }

    suspend fun updateProfile(profileId: Int, profileName: String) {
        db.profileDao().updateProfileName(profileId, profileName)
    }

    suspend fun updateProfileName(currentName: String, updatedName: String) {
        db.profileDao().updateProfileName(currentName, updatedName)
    }

    fun activeProfile() = db.activeProfileDao().activeProfile()
    fun getProfileById(profileId: Int) = db.profileDao().loadProfile(profileId)

    fun removeLastModifiedTaskDate(profileName: String) {
        securePrefs.edit().remove(profileName).apply()
    }

    suspend fun updateProfileColor(profileName: String, color: ProfileColors) {
        db.profileDao().updateProfileColor(profileName, color)
    }

    fun lastAuthenticatedDate(profileId: Int) =
        db.profileDao().getLastAuthenticated(profileId)

    suspend fun updateLastAuthenticated(validOn: Instant, profileName: String) =
        db.profileDao().updateLastAuthenticated(validOn, profileName)
}
