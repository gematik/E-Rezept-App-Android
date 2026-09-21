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

package de.gematik.ti.erp.app.database.api

import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ProfileLocalDataSource {
    fun loadProfiles(): Flow<List<ProfileErpModel>>

    fun activeProfile(): Flow<ProfileErpModel?>

    fun getProfileById(profileId: String): Flow<ProfileErpModel?>

    suspend fun saveProfile(profile: ProfileErpModel)

    suspend fun deleteProfile(profileId: String)

    suspend fun activateProfile(profileId: String)
    suspend fun deactivateAllProfiles()

    suspend fun updateProfileName(profileId: String, profileName: String)

    suspend fun updateProfileColor(profileId: String, color: ProfileColorNames)

    suspend fun saveAvatarFigure(profileId: String, avatar: Avatar)

    suspend fun savePersonalizedProfileImage(profileId: String, profileImage: ByteArray)

    suspend fun clearPersonalizedProfileImage(profileId: String)

    suspend fun updateInsuranceInformation(
        profileId: String,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    )

    suspend fun updateInsuranceType(profileId: String, insuranceType: InsuranceType)

    suspend fun updateOrganizationIdentifier(profileId: String, organizationIdentifier: String)

    suspend fun updateLastAuthenticated(profileId: String, lastAuthenticated: Instant)
    suspend fun updateLastTaskSynced(profileId: String, lastTaskSynced: Instant)

    suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean
}
