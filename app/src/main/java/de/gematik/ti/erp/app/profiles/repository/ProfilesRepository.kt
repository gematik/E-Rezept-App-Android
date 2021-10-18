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

import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.Profile
import javax.inject.Inject

class ProfilesRepository @Inject constructor(
    private val db: AppDatabase
) {
    fun profiles() =
        db.profileDao().getAllProfiles()

    suspend fun saveProfile(profile: Profile) {
        db.profileDao().insertProfile(profile)
    }

    suspend fun deleteProfile(profile: Profile) {
        db.profileDao().deleteProfile(profile)
    }

    suspend fun insertActiveProfile(profileName: String) {
        db.activeProfileDao().insertActiveSession(ActiveProfile(profileName = profileName))
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
}
