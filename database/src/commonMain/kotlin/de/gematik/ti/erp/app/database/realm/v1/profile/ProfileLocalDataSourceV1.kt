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

package de.gematik.ti.erp.app.database.realm.v1.profile

import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class ProfileLocalDataSourceV1(
    private val realm: Realm
) : ProfileLocalDataSource {
    override fun loadProfiles(): Flow<List<ProfileErpModel>> =
        realm.query<ProfileEntityV1>().asFlow().map { query ->
            query.list.map { it.toProfileErpModel() }
        }

    override fun activeProfile(): Flow<ProfileErpModel?> =
        realm.query<ProfileEntityV1>("active == true").asFlow().map { query ->
            query.list.firstOrNull()?.toProfileErpModel()
        }

    override fun getProfileById(profileId: String): Flow<ProfileErpModel?> =
        realm.query<ProfileEntityV1>("id == $0", profileId).asFlow().map { query ->
            query.list.firstOrNull()?.toProfileErpModel()
        }

    override suspend fun saveProfile(profile: ProfileErpModel) {
        realm.write {
            copyToRealm(profile.toRealmEntity(), UpdatePolicy.ALL)
        }
    }

    override suspend fun deleteProfile(profileId: String) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.let { profile ->
                delete(profile)
            }
        }
    }

    override suspend fun activateProfile(profileId: String) {
        realm.write {
            query<ProfileEntityV1>().find().forEach { it.active = false }
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                active = true
            }
        }
    }

    override suspend fun deactivateAllProfiles() {
        realm.write {
            query<ProfileEntityV1>().find().forEach { it.active = false }
        }
    }

    override suspend fun updateProfileName(profileId: String, profileName: String) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                name = profileName
                isNewlyCreated = false
            }
        }
    }

    override suspend fun updateProfileColor(profileId: String, color: ProfileColorNames) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                this.color = color.toRealmEnum()
            }
        }
    }

    override suspend fun saveAvatarFigure(profileId: String, avatar: Avatar) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                avatarFigure = avatar.toRealmEnum()
            }
        }
    }

    override suspend fun savePersonalizedProfileImage(profileId: String, profileImage: ByteArray) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                personalizedImage = profileImage
            }
        }
    }

    override suspend fun clearPersonalizedProfileImage(profileId: String) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                personalizedImage = null
            }
        }
    }

    override suspend fun updateInsuranceInformation(
        profileId: String,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    ) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                this.insurantName = insurantName
                this.insuranceIdentifier = insuranceIdentifier
                this.organizationIdentifier = organizationIdentifier
                this.insuranceName = insuranceName
                if (this.isNewlyCreated) {
                    this.name = insurantName
                }
            }
        }
    }

    override suspend fun updateInsuranceType(profileId: String, insuranceType: InsuranceType) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                this.insuranceType = insuranceType.toRealmEnum()
            }
        }
    }

    override suspend fun updateOrganizationIdentifier(profileId: String, organizationIdentifier: String) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                this.organizationIdentifier = organizationIdentifier
            }
        }
    }

    override suspend fun updateLastAuthenticated(profileId: String, lastAuthenticated: Instant) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                this.lastAuthenticated = lastAuthenticated.toRealmInstant()
            }
        }
    }

    override suspend fun updateLastTaskSynced(profileId: String, lastTaskSynced: Instant) {
        realm.write {
            queryFirst<ProfileEntityV1>("id == $0", profileId)?.apply {
                this.lastTaskSynced = lastTaskSynced.toRealmInstant()
            }
        }
    }

    override suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean =
        realm.queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
            profile.lastAuthenticated != null
        } ?: false
}
