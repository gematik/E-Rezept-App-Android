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

package de.gematik.ti.erp.app.profiles.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.AuditEventWithMedicationText
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.settings.usecase.DEFAULT_PROFILE_NAME
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val eventsPerPage = 20

class KVNRAlreadyAssignedException(
    message: String,
    val isActiveProfile: Boolean,
    val inProfile: String,
    val insuranceIdentifier: String
) : IllegalStateException(message)

class ProfilesRepository @Inject constructor(
    private val db: AppDatabase,
    dispatcher: DispatchProvider
) {
    private val scope = CoroutineScope(dispatcher.io())

    init {
        scope.launch {
            if (db.activeProfileDao().activeProfile() == null) {
                db.profileDao().insertProfile(ProfileEntity(name = DEFAULT_PROFILE_NAME))
                db.activeProfileDao().insertActiveProfile(ActiveProfile(profileName = DEFAULT_PROFILE_NAME))
            }
        }
    }

    fun profiles() =
        db.profileDao().getAllProfilesFlow()

    suspend fun saveProfile(profileName: String, activate: Boolean = false) {
        db.withTransaction {
            db.profileDao().insertProfile(ProfileEntity(name = profileName))
            if (activate) {
                db.activeProfileDao().updateActiveProfile(profileName)
            }
        }
    }

    suspend fun updateProfileByName(currentName: String, updatedName: String, activate: Boolean = false) {
        db.withTransaction {
            db.profileDao().updateProfileName(currentName, updatedName)
            if (activate || profileIsActive(currentName)) {
                db.activeProfileDao().updateActiveProfile(updatedName)
            }
        }
    }

    private suspend fun profileIsActive(profileName: String) =
        db.activeProfileDao().activeProfile()?.profileName == profileName

    suspend fun removeProfile(profileName: String) {
        db.withTransaction {
            if (profileIsActive(profileName)) {
                val profiles = db.profileDao().getAllProfiles()
                if (profiles.size == 1) {
                    error("Can't remove the last profile!")
                } else {
                    saveProfile(profiles.find { it.name != profileName }!!.name, activate = true)
                }
            }
            db.profileDao().removeProfileByName(profileName)
        }
    }

    fun activeProfile() =
        db.activeProfileDao().activeProfileFlow().filterNotNull()

    fun getProfileById(profileId: Int) = db.profileDao().loadProfile(profileId)

    suspend fun updateProfileColor(profileName: String, color: ProfileColorNames) {
        db.profileDao().updateProfileColor(profileName, color)
    }

    suspend fun updateLastAuthenticated(validOn: Instant, profileName: String) =
        db.profileDao().updateLastAuthenticated(validOn, profileName)

    fun loadAuditEventsForProfile(profileName: String): Flow<PagingData<AuditEventWithMedicationText>> {
        return Pager(
            PagingConfig(
                pageSize = eventsPerPage,
                enablePlaceholders = false
            ),
            pagingSourceFactory = db.taskDao().getAuditEventsForProfileName(profileName).asPagingSourceFactory()
        ).flow
    }

    suspend fun setInsuranceInformation(
        profileName: String,
        insurantName: String,
        insuranceIdentifier: String,
        insuranceName: String
    ) {
        db.profileDao().getAllProfiles().let { profiles ->
            profiles.find { it.insuranceIdentifier == insuranceIdentifier && it.name != profileName }
                ?.let {
                    throw KVNRAlreadyAssignedException(
                        "KVNR already assigned to another profile",
                        false,
                        it.name,
                        it.insuranceIdentifier!!
                    )
                }
            profiles.find { it.name == profileName }
                ?.takeIf {
                    it.insuranceIdentifier != null && it.insuranceIdentifier != insuranceIdentifier
                }
                ?.let {
                    throw KVNRAlreadyAssignedException(
                        "Profile already assigned to another KVNR",
                        true,
                        profileName,
                        it.insuranceIdentifier!!
                    )
                }
        }
        db.profileDao().setInsuranceInformation(profileName, insurantName, insuranceIdentifier, insuranceName)
    }
}
